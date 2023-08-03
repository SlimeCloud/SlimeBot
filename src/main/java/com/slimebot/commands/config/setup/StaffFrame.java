package com.slimebot.commands.config.setup;

import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.commands.config.engine.ConfigFieldType;
import com.slimebot.commands.config.setup.engine.CustomSetupFrame;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.StaffConfig;
import com.slimebot.message.StaffMessage;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import de.mineking.discord.ui.components.button.FrameButton;
import de.mineking.discord.ui.components.select.EntitySelectComponent;
import de.mineking.discord.ui.components.select.StringSelectComponent;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.stream.Collectors;

public class StaffFrame {
	public static class StaffChannelFrame extends CustomSetupFrame {
		public StaffChannelFrame(Menu menu, long guild) {
			super("staff channel", menu, guild, ConfigFieldType.CHANNEL.getEmoji() + " Staff-Kanal", "In diesem Kanal wird die Team-Nachricht gesendet und bearbeitet",
					config -> config.getStaffConfig().flatMap(StaffConfig::getChannel).map(Channel::getAsMention)
			);

			addComponents(
					new EntitySelectComponent("channel",
							config -> config
									.setPlaceholder("Kanal festlegen")
									.setChannelTypes(ChannelType.TEXT, ChannelType.NEWS),
							EntitySelectMenu.SelectTarget.CHANNEL
					).handle((m, evt) -> {
						GuildConfig.getConfig(guild).getStaffConfig().ifPresent(staff ->
								staff.getChannel().ifPresent(ch -> {
									if (staff.message != null) {
										ch.deleteMessageById(staff.message).queue();
									}
								})
						);

						ConfigCommand.updateField(guild, config -> {
							StaffConfig staff = config.getOrCreateStaff();

							staff.channel = evt.getValues().get(0).getIdLong();
							staff.message = null;
						});

						StaffMessage.updateMessage(evt.getGuild());

						menu.update();
					})
			);

			addComponents(
					ComponentRow.of(
							new ButtonComponent("reset", ButtonColor.RED, "Wert zurücksetzten").handle((m, evt) -> {
								ConfigCommand.updateField(guild, config -> config.getStaffConfig().ifPresent(staff -> staff.channel = null)); //Keep role configuration to make it easier to re-enable the feature
								menu.update();
							}),
							new FrameButton(ButtonColor.GRAY, "Zurück", "main"),
							new FrameButton(ButtonColor.GRAY, "Weiter", "staff roles")
					)
			);
		}
	}

	public static class StaffRolesFrame extends CustomSetupFrame {
		private Long role;

		public StaffRolesFrame(Menu menu, long guild) {
			super("staff roles", menu, guild, ConfigFieldType.ROLE.getEmoji() + " Team-Rollen festlegen", "Diese Rollen vergeben keine Rechte, sondern sind diejenigen, die in der Team-Nachricht nagezeigt werden",
					config -> config.getStaffConfig().map(staff -> staff.roles.entrySet().stream()
							.map(e -> {
								try {
									return "> " + Main.jdaInstance.getRoleById(e.getKey()).getAsMention() + " " + e.getValue();
								} catch (NumberFormatException x) {
									return "> " + e.getValue();
								}
							}).collect(Collectors.joining("\n"))
					)
			);

			menu.addModalFrame("staff role description", "Beschreibung der Rolle",
					modal -> modal
							.addActionRow(
									TextInput.create("description", "Beschreibung", TextInputStyle.SHORT)
											.build()
							),
					(m, evt) -> {
						ConfigCommand.updateField(evt.getGuild(), config -> config.getOrCreateStaff().roles.put(role.toString(), evt.getValue("description").getAsString()));
						StaffMessage.updateMessage(evt.getGuild());

						m.display("staff roles");
					}
			);

			addComponents(
					new StringSelectComponent("remove", select -> {
						select.setPlaceholder("Rolle entfernen");

						StaffConfig staff = GuildConfig.getConfig(guild).getOrCreateStaff();

						if (staff.roles.isEmpty()) {
							select.addOption("---", "---"); //SelectMenus cannot be empty
						}

						else {
							select.addOptions(
									staff.roles.entrySet().stream()
											.map(e -> {
												try {
													return SelectOption.of(Main.jdaInstance.getRoleById(e.getKey()).getName(), e.getKey())
															.withDescription(e.getValue())
															.withEmoji(Emoji.fromFormatted(ConfigFieldType.ROLE.getEmoji()));
												} catch (NumberFormatException ex) {
													return SelectOption.of(e.getKey(), e.getKey())
															.withDescription(e.getValue().substring(0, Math.min(100, e.getValue().length())))
															.withEmoji(Emoji.fromFormatted("\uD83D\uDCDD"));
												}
											})
											.toList()
							);
						}
					}).handle((m, evt) -> {
						ConfigCommand.updateField(evt.getGuild(), config -> config.getOrCreateStaff().roles.remove(evt.getSelectedOptions().get(0).getValue()));
						StaffMessage.updateMessage(evt.getGuild());

						m.update();
					}).asDisabled(() -> GuildConfig.getConfig(guild).getStaffConfig().map(staff -> staff.roles.isEmpty()).orElse(true)),
					new EntitySelectComponent("add",
							select -> select.setPlaceholder("Rolle hinzufügen"),
							EntitySelectMenu.SelectTarget.ROLE
					).handle((m, evt) -> {
						role = evt.getValues().get(0).getIdLong();
						m.display("staff role description");
					})
			);

			addComponents(
					ComponentRow.of(
							new FrameButton(ButtonColor.GRAY, "Zurück", "staff channel"),
							new FrameButton(ButtonColor.GRAY, "Weiter", "main")
					)
			);
		}
	}
}
