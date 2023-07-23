package com.slimebot.commands;

import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.main.CommandPermission;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.SpotifyNotificationConfig;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.ui.CallbackState;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import de.mineking.discord.ui.components.button.FrameButton;
import de.mineking.discord.ui.components.select.EntitySelectComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@ApplicationCommand(name = "setup", description = "Startet einen Setup-Wizard, mit dem eine initiale Konfiguration erstellt werden kann", guildOnly = true)
public class SetupCommand {
	public CommandPermission permission = CommandPermission.TEAM;

	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event) {
		showSetupMenu(event);
	}

	public static void showSetupMenu(IReplyCallback event) {
		Menu menu = Main.discordUtils.getUIManager().createMenu()
				.addMessageFrame("main", () ->
								new EmbedBuilder()
										.setColor(GuildConfig.getColor(event.getGuild()))
										.setTitle("\uD83D\uDD27 Einstellungs-Menü")
										.setDescription("Wähle aus, welchen Bereich zu konfigurieren möchtest oder schließe das Menü")
										.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
										.build(),
						frame -> frame.addComponents(
								ComponentRow.of(
										new FrameButton(ButtonColor.GRAY, "Haupteinstellungen", "color"),
										new FrameButton(ButtonColor.GRAY, "Spotify Benachrichtigungen", "spotify_music")
								),
								new ButtonComponent("finish", ButtonColor.RED, "Schließen").handle((m, evt) -> m.close())
						)
				);

		mainConfig(event, menu);
		spotifyConfig(event, menu);

		menu.start(new CallbackState(event), "main");
	}

	public static void spotifyConfig(IReplyCallback event, Menu menu) {
		entitySelect(event, menu, "spotify_music", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle einen Musik-Kanal",
				"In diesem Kanal werden Benachrichtigungen über neue Musik-Releases gesendet",
				(config, value) -> config.getOrCreateSpotify().musicChannel = value,
				config -> config.getSpotify().flatMap(SpotifyNotificationConfig::getMusicChannel),
				"main", "spotify_podcast"
		);

		entitySelect(event, menu, "spotify_podcast", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle einen Podcast-Kanal",
				"In diesem Kanal werden Benachrichtigungen über neue Podcast Episoden gesendet",
				(config, value) -> config.getOrCreateSpotify().podcastChannel = value,
				config -> config.getSpotify().flatMap(SpotifyNotificationConfig::getPodcastChannel),
				"spotify_music", "spotify_role"
		);

		entitySelect(event, menu, "spotify_role", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle eine Benachrichtigungs-Rolle",
				"Diese Rolle wird bei Spotify Nachrichten erwähnt",
				(config, value) -> config.getOrCreateSpotify().notificationRole = value,
				config -> config.getSpotify().flatMap(SpotifyNotificationConfig::getRole),
				"spotify_podcast", "main"
		);
	}

	public static void mainConfig(IReplyCallback event, Menu menu) {
		menu.addMessageFrame("color", () ->
								new EmbedBuilder()
										.setColor(GuildConfig.getColor(event.getGuild()))
										.setTitle("\uD83C\uDFA8 Farbe")
										.setDescription("Wähle zunächst eine Farbe, die für Embeds verwendet wird. Du kannst bei google einfach 'color picker' eingeben, um eine Farbe zu wählen und ihren HEX-Code zu bekommen")
										.addField("Aktueller Wert", Optional.ofNullable(GuildConfig.getConfig(event.getGuild()).color).orElse(Main.config.color), false)
										.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
										.build(),
						frame -> frame.addComponents(
								new ButtonComponent("color", ButtonColor.BLUE, "Farbe wählen").handle((m, evt) -> m.display("colorModal")),
								ComponentRow.of(
										new FrameButton(ButtonColor.GRAY, "Zurück", "main"),
										new FrameButton(ButtonColor.GRAY, "Überspringen", "logChannel")
								)
						)
				)
				.addModalFrame("colorModal", "Wähle eine Farbe",
						modal -> modal.addActionRow(
								TextInput.create("color", "Die Farbe, die für Embeds verwendet wird.", TextInputStyle.SHORT)
										.setPlaceholder("HEX-Code der Farbe")
										.setMaxLength(7)
										.setMinLength(7)
										.setValue(Main.config.color)
										.build()
						),
						(m, evt) -> {
							String input = evt.getValue("color").getAsString();

							try {
								Color.decode(input);

								ConfigCommand.updateField(evt.getGuild(), config -> config.color = input);

								m.display("logChannel");
							} catch(NumberFormatException e) {
								m.display("color");
								evt.getHook().sendMessage("Ungültige Farbe!").setEphemeral(true).queue();
							}
						}
				);

		entitySelect(event, menu, "logChannel", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle einen Log-Kanal",
				"In diesem Kanal werden Informationen bezüglich des Bots gesendet",
				(config, value) -> config.logChannel = value,
				GuildConfig::getLogChannel,
				"color", "greetingsChannel"
		);

		entitySelect(event, menu, "greetingsChannel", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle einen Gruß-Kanal",
				"In diesem Kanal werden Gruß-Nachrichten - wie z.B. zu Ferien-Beginnen - gesendet",
				(config, value) -> config.greetingsChannel = value,
				GuildConfig::getGreetingsChannel,
				"logChannel", "punishmentChannel"
		);

		entitySelect(event, menu, "punishmentChannel", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle einen Straf-Kanal",
				"In diesem Kanal werden Informationen über Bestrafungen gesendet",
				(config, value) -> config.punishmentChannel = value,
				GuildConfig::getPunishmentChannel,
				"greetingsChannel", "staffRole"
		);

		entitySelect(event, menu, "staffRole", EntitySelectMenu.SelectTarget.ROLE,
				"Wähle eine Team-Rolle",
				"Mitglieder mit dieser Rolle haben Zugriff auf beschränkte Befehle",
				(config, value) -> config.staffRole = value,
				GuildConfig::getStaffRole,
				"punishmentChannel", "contributorRole"
		);

		entitySelect(event, menu, "contributorRole", EntitySelectMenu.SelectTarget.ROLE,
				"Wähle eine Contributor-Rolle",
				"Diese Rolle kann von Mitgliedern beantragt werden, wenn sie bei diesem Bot auf GitHub mitgearbeitet haben",
				(config, value) -> config.contributorRole = value,
				GuildConfig::getContributorRole,
				"staffRole", "main"
		);

	}

	private static void entitySelect(IReplyCallback event, Menu menu, String id, EntitySelectMenu.SelectTarget target, String title, String description,
	                                 BiConsumer<GuildConfig, Long> handler, Function<GuildConfig, Optional<? extends IMentionable>> supplier,
	                                 String previous, String next
	) {
		List<ButtonComponent> buttons = new ArrayList<>();

		buttons.add(new ButtonComponent("reset", ButtonColor.RED, "Wert zurücksetzten").handle((m, evt) -> {
			ConfigCommand.updateField(evt.getGuild(), config -> handler.accept(config, null));
			m.update();
		}));

		buttons.add(new FrameButton(ButtonColor.GRAY, "Zurück", previous));

		if(!previous.equals("main") && !next.equals("main")) {
			buttons.add(new FrameButton(ButtonColor.GRAY, "Hauptmenü", "main"));
		}

		buttons.add(new FrameButton(ButtonColor.GRAY, "Überspringen", next));

		menu.addMessageFrame(id, () ->
						new EmbedBuilder()
								.setColor(GuildConfig.getColor(event.getGuild()))
								.setTitle((target == EntitySelectMenu.SelectTarget.CHANNEL ? "\uD83D\uDCDD" : "\uD83E\uDDFB") + " " + title)
								.setDescription(description)
								.addField("Aktueller Wert", supplier.apply(GuildConfig.getConfig(event.getGuild())).map(IMentionable::getAsMention).orElse("*Keiner*"), false)
								.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
								.build(),
				frame -> frame.addComponents(
						new EntitySelectComponent(id,
								select -> select.setChannelTypes(ChannelType.TEXT, ChannelType.NEWS),
								target
						).handle((m, evt) -> {
							ConfigCommand.updateField(evt.getGuild(), config -> handler.accept(config, evt.getValues().get(0).getIdLong()));
							m.display(next);
						}),
						ComponentRow.of(buttons)
				)
		);
	}
}
