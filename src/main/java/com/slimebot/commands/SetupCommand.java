package com.slimebot.commands;

import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.main.CommandPermission;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.FdmdsConfig;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.SpotifyNotificationConfig;
import com.slimebot.main.config.guild.StaffConfig;
import com.slimebot.message.StaffMessage;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.ui.CallbackState;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import de.mineking.discord.ui.components.button.FrameButton;
import de.mineking.discord.ui.components.select.EntitySelectComponent;
import de.mineking.discord.ui.components.select.StringSelectComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

@ApplicationCommand(name = "setup", description = "Startet einen Setup-Wizard, mit dem eine initiale Konfiguration erstellt werden kann", guildOnly = true)
public class SetupCommand {
	public CommandPermission permission = CommandPermission.TEAM;

	public final static String roleEmoji = "\uD83E\uDDFB";
	public final static String channelEmoji = "\uD83D\uDCDD";
	public final static String commentEmoji = "\uD83D\uDCDD";

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
										new FrameButton(ButtonColor.GRAY, "Spotify Benachrichtigungen", "spotifyMusic"),
										new FrameButton(ButtonColor.GRAY, "Team-Nachricht", "staffChannel"),
										new FrameButton(ButtonColor.GRAY, "Frag doch mal den Schleim", "fdmdsLogChannel")
								),
								new ButtonComponent("finish", ButtonColor.RED, "Schließen").handle((m, evt) -> m.close())
						)
				);

		mainConfig(event, menu);
		spotifyConfig(event, menu);
		staffConfig(event, menu);
		fdmdsConfig(event, menu);

		menu.start(new CallbackState(event), "main");
	}

	public static void fdmdsConfig(IReplyCallback event, Menu menu) {
		entitySelect(event, menu, "fdmdsLogChannel", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle einen Log-Kanal",
				"In diesen Kanal werden alle Fdmds Einreichungen gesendet, um vom Team Bestätigt oder bearbeitet zu werden",
				(config, value) -> config.getOrCreateFdmds().logChannel = value,
				config -> config.getFdmds().flatMap(FdmdsConfig::getLogChannel),
				"main", "fdmdsChannel"
		);

		entitySelect(event, menu, "fdmdsChannel", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle einen Fdmds-Kanal",
				"In diesen Kanal werden bestätigte Fragen geschickt",
				(config, value) -> config.getOrCreateFdmds().channel = value,
				config -> config.getFdmds().flatMap(FdmdsConfig::getChannel),
				"fdmdsLogChannel", "fdmdsRole"
		);

		entitySelect(event, menu, "fdmdsRole", EntitySelectMenu.SelectTarget.ROLE,
				"Wähle eine Benachrichtigungs-Rolle",
				"Diese Rolle wird bei bei neuen Fdmds Fragen erwähnt",
				(config, value) -> config.getOrCreateFdmds().role = value,
				config -> config.getFdmds().flatMap(FdmdsConfig::getRole),
				"fdmdsChannel", "main"
		);
	}

	public static void staffConfig(IReplyCallback event, Menu menu) {
		entitySelect(event, menu, "staffChannel", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle einen Kanal für die Team-Nachricht",
				"In diesem Kanal wird eine Nachricht gesendet, die alle Team-Rollen mit deren Mitgliedern anzeigt",
				(config, value) -> {
					config.getStaffConfig().ifPresent(staff ->
							staff.getChannel().ifPresent(channel -> {
								if(staff.message != null) {
									channel.deleteMessageById(staff.message).queue();
								}
							})
					);

					StaffConfig staff = config.getOrCreateStaff();
					staff.channel = value;
					staff.message = null;

					StaffMessage.updateMessage(event.getGuild());
				},
				config -> config.getStaffConfig().flatMap(StaffConfig::getChannel),
				"main", "staffRoles"
		);

		AtomicReference<Long> role = new AtomicReference<>();

		menu.addMessageFrame("staffRoles", () ->
				new EmbedBuilder()
						.setColor(GuildConfig.getColor(event.getGuild()))
						.setTitle("\uD83E\uDDFB Rollen festlegen")
						.setDescription("Füge mit dem Menü unter der Nachricht Team-Rollen hinzu. Um Die Reihenfolge zu ändern oder Text-Zeilen ein zu fügen, muss der Bot-Administrator manuell die Konfigurationsdatei bearbeiten.")
						.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
						.build(),
				frame -> frame.addComponents(
						new StringSelectComponent("remove", select -> {
							select.setPlaceholder("Rolle entfernen");

							StaffConfig staff = GuildConfig.getConfig(event.getGuild()).getOrCreateStaff();

							if(staff.roles.isEmpty()) {
								select.addOption("---", "---"); //SelectMenus cannot be empty
							}

							else {
								select.addOptions(
										staff.roles.entrySet().stream()
												.map(e -> {
													try {
														return SelectOption.of(Main.jdaInstance.getRoleById(e.getKey()).getName(), e.getKey())
																.withDescription(e.getValue())
																.withEmoji(Emoji.fromFormatted(roleEmoji));
													} catch(NumberFormatException ex) {
														return SelectOption.of(e.getKey(), e.getKey())
																.withDescription(e.getValue())
																.withEmoji(Emoji.fromFormatted(commentEmoji));
													}
												})
												.toList()
								);
							}
						}).handle((m, evt) -> {
							ConfigCommand.updateField(evt.getGuild(), config -> config.getOrCreateStaff().roles.remove(evt.getSelectedOptions().get(0).getValue()));
							StaffMessage.updateMessage(evt.getGuild());

							m.update();
						}).asDisabled(() -> GuildConfig.getConfig(event.getGuild()).getStaffConfig().map(staff -> staff.roles.isEmpty()).orElse(true)),
						new EntitySelectComponent("add",
								select -> select.setPlaceholder("Rolle hinzufügen"),
								EntitySelectMenu.SelectTarget.ROLE
						).handle((m, evt) -> {
							role.set(evt.getValues().get(0).getIdLong());
							m.display("staffRoleDescription");
						}),
						ComponentRow.of(
								new FrameButton(ButtonColor.GRAY, "Zurück", "staffChannel"),
								new FrameButton(ButtonColor.GRAY, "Hauptmenü", "main")
						)
				)
		);

		menu.addModalFrame("staffRoleDescription", "Beschreibung der Rolle",
				modal -> modal
						.addActionRow(
								TextInput.create("description", "Beschreibung", TextInputStyle.SHORT)
										.build()
						),
				(m, evt) -> {
					ConfigCommand.updateField(evt.getGuild(), config -> config.getOrCreateStaff().roles.put(role.get().toString(), evt.getValue("description").getAsString()));
					StaffMessage.updateMessage(evt.getGuild());

					m.display("staffRoles");
				}
		);
	}

	public static void spotifyConfig(IReplyCallback event, Menu menu) {
		entitySelect(event, menu, "spotifyMusic", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle einen Musik-Kanal",
				"In diesem Kanal werden Benachrichtigungen über neue Musik-Releases gesendet",
				(config, value) -> config.getOrCreateSpotify().musicChannel = value,
				config -> config.getSpotify().flatMap(SpotifyNotificationConfig::getMusicChannel),
				"main", "spotifyPodcast"
		);

		entitySelect(event, menu, "spotifyPodcast", EntitySelectMenu.SelectTarget.CHANNEL,
				"Wähle einen Podcast-Kanal",
				"In diesem Kanal werden Benachrichtigungen über neue Podcast Episoden gesendet",
				(config, value) -> config.getOrCreateSpotify().podcastChannel = value,
				config -> config.getSpotify().flatMap(SpotifyNotificationConfig::getPodcastChannel),
				"spotifyMusic", "spotifyRole"
		);

		entitySelect(event, menu, "spotifyRole", EntitySelectMenu.SelectTarget.ROLE,
				"Wähle eine Benachrichtigungs-Rolle",
				"Diese Rolle wird bei Spotify Nachrichten erwähnt",
				(config, value) -> config.getOrCreateSpotify().notificationRole = value,
				config -> config.getSpotify().flatMap(SpotifyNotificationConfig::getRole),
				"spotifyPodcast", "main"
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
								.setTitle((target == EntitySelectMenu.SelectTarget.CHANNEL ? channelEmoji : roleEmoji) + " " + title)
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
