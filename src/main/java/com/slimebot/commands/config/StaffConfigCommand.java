package com.slimebot.commands.config;

import com.slimebot.main.Main;
import com.slimebot.message.StaffMessage;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "staff", description = "Konfiguration der automatischen Staff-Nachricht")
public class StaffConfigCommand {
	@ApplicationCommand(name = "channel", description = "Setzt den Kanal der Staff-Nachricht")
	public static class ChannelCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "kanal", description = "Der neue Kanal für die Staff-Nachricht", channelTypes = {ChannelType.TEXT, ChannelType.NEWS}, required = false) GuildMessageChannel channel
		) {
			if(channel == null) {
				ConfigCommand.updateField(event.getGuild(), config -> config.getStaffConfig().ifPresent(staff -> staff.channel = null)); //Keep role configuration to make it easier to re-enable the feature
				Main.database.run(handle -> handle.createUpdate("delete from staff_config where guild = :guild")
						.bind("guild", event.getGuild().getIdLong())
						.execute()
				);

				event.reply("Kanal erfolgreich zurückgesetzt").setEphemeral(true).queue();
			}

			else {
				if(!channel.getGuild().equals(event.getGuild())) {
					event.reply("Der Kanal ist nicht auf diesem Server!").setEphemeral(true).queue();
					return;
				}

				ConfigCommand.updateField(event.getGuild(), config -> config.getOrCreateStaff().channel = channel.getIdLong());

				StaffMessage.updateMessage(event.getGuild());

				event.reply("Staff-Kanal erfolgreich auf " + channel.getAsMention() + " gesetzt").setEphemeral(true).queue();
			}
		}
	}

	@ApplicationCommand(name = "add_role", description = "Fügt eine neue Staff-Rolle hinzu")
	public static class AddRoleCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "rolle", description = "Die Staff-Rolle") Role role,
		                           @Option(name = "beschreibung", description = "Die Beschreibung der Rolle") String description
		) {
			ConfigCommand.updateField(event.getGuild(), config -> config.getStaffConfig().ifPresent(staff -> staff.roles.put(role.getId(), description)));

			StaffMessage.updateMessage(event.getGuild());

			event.reply(role.getAsMention() + " mit Beschreibung `" + description + "` als Staff-Rolle hinzugefügt").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "remove_role", description = "Entfernt eine Staff-Rolle")
	public static class RemoveRoleCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "rolle", description = "Die Staff-Rolle") Role role
		) {
			ConfigCommand.updateField(event.getGuild(), config -> config.getStaffConfig().ifPresent(staff -> staff.roles.remove(role.getId())));

			StaffMessage.updateMessage(event.getGuild());

			event.reply(role.getAsMention() + " ist nun keine Staff-Rolle mehr").setEphemeral(true).queue();
		}
	}
}
