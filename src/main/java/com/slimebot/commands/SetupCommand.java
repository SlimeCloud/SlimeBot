package com.slimebot.commands;

import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.main.CommandPermission;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.ui.CallbackState;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import de.mineking.discord.ui.components.select.EntitySelectComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.awt.*;
import java.util.function.BiConsumer;

@ApplicationCommand(name = "setup", description = "Startet einen Setup-Wizard, mit dem eine initiale Konfiguration erstellt werden kann", guildOnly = true)
public class SetupCommand {
	public CommandPermission permission = CommandPermission.TEAM;

	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event) {
		showSetupMenu(event);
	}

	public static void showSetupMenu(IReplyCallback event) {
		Menu menu = Main.discordUtils.getUIManager().createMenu()
				.addMessageFrame("color", () ->
								new EmbedBuilder()
										.setColor(GuildConfig.getColor(event.getGuild()))
										.setTitle("\uD83C\uDFA8 Farbe")
										.setDescription("Wähle zunächst eine Farbe, die für Embeds verwendet wird. Du kannst bei google einfach 'color picker' eingeben, um eine Farbe zu wählen und ihren HEX-Code zu bekommen")
										.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
										.build(),
						frame -> frame.addComponents(
								ComponentRow.of(
										new ButtonComponent("color", ButtonColor.BLUE, "Farbe wählen").handle((m, evt) -> m.display("colorModal")),
										new ButtonComponent("skip", ButtonColor.GRAY, "Überspringen").handle((m, evt) -> m.display("logChannel"))
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
				)
				.addMessageFrame("finish", () ->
						new EmbedBuilder()
								.setColor(GuildConfig.getColor(event.getGuild()))
								.setTitle("☑️ Fertigstellen")
								.setDescription("Einrichtung abgeschlossen, du kannst das Menü jetzt schließen")
								.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
								.build(),
						frame -> frame.addComponents(
								new ButtonComponent("finish", ButtonColor.GREEN, "Schließen").handle((m, evt) -> m.close())
						)
				);

		entitySelect(event, menu, "logChannel", EntitySelectMenu.SelectTarget.CHANNEL,
				"\uD83D\uDCDD Wählen einen Log-Kanal",
				"In diesem Kanal werden Logs über dem Bot gesendet", //TODO Better description
				(config, value) -> config.logChannel = value.getIdLong(),
				"color", "greetingsChannel"
		);

		entitySelect(event, menu, "greetingsChannel", EntitySelectMenu.SelectTarget.CHANNEL,
				"\uD83D\uDCDD Wählen einen Gruß-Kanal",
				"In diesem Kanal werden Gruß-Nachrichten - wie z.B. zu Ferien-Beginnen - gesendet",
				(config, value) -> config.greetingsChannel = value.getIdLong(),
				"logChannel", "punishmentChannel"
		);

		entitySelect(event, menu, "punishmentChannel", EntitySelectMenu.SelectTarget.CHANNEL,
				"\uD83D\uDCDD Wähle einen Straf-Kanal",
				"In diesem Kanal werden Informationen über Bestrafungen gesendet",
				(config, value) -> config.punishmentChannel = value.getIdLong(),
				"greetingsChannel", "staffRole"
		);

		entitySelect(event, menu, "staffRole", EntitySelectMenu.SelectTarget.ROLE,
				"\uD83E\uDDFB Wählen eine Team-Rolle",
				"Mitglieder mit dieser Rolle haben Zugriff auf beschränkte Befehle",
				(config, value) -> config.staffRole = value.getIdLong(),
				"punishmentChannel", "contributorRole"
		);

		entitySelect(event, menu, "contributorRole", EntitySelectMenu.SelectTarget.ROLE,
				"\uD83E\uDDFB Wähle eine Contributor-Rolle",
				"Diese Rolle kann von Mitgliedern beantragt werden, wenn sie bei diesem Bot auf GitHub mitgearbeitet haben",
				(config, value) -> config.contributorRole = value.getIdLong(),
				"staffRole", "finish"
		);

		menu.start(new CallbackState(event), "color");
	}

	private static void entitySelect(IReplyCallback event, Menu menu, String id, EntitySelectMenu.SelectTarget target, String title, String description, BiConsumer<GuildConfig, ISnowflake> handler, String previous, String next) {
		menu.addMessageFrame(id, () ->
						new EmbedBuilder()
								.setColor(GuildConfig.getColor(event.getGuild()))
								.setTitle(title)
								.setDescription(description)
								.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
								.build(),
				frame -> frame.addComponents(
						new EntitySelectComponent(id,
								select -> select.setChannelTypes(ChannelType.TEXT, ChannelType.NEWS),
								target
						).handle((m, evt) -> {
							ConfigCommand.updateField(evt.getGuild(), config -> handler.accept(config, evt.getValues().get(0)));
							m.display(next);
						}),
						ComponentRow.of(
								new ButtonComponent("back", ButtonColor.GRAY, "Zurück").handle((m, evt) -> m.display(previous)),
								new ButtonComponent("skip", ButtonColor.GRAY, "Überspringen").handle((m, evt) -> m.display(next))
						)
				)
		);
	}
}
