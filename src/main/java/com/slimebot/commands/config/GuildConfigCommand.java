package com.slimebot.commands.config;

import com.slimebot.main.DatabaseField;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@ApplicationCommand(name = "guild", description = "Konfiguriert grundlegende Funktionen des Servers")
public class GuildConfigCommand {
	@ApplicationCommand(name = "color", description = "Ändert die Farbe für Embeds")
	public static class ColorCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "color", description = "Der HEX-Code für die Farbe", required = false) String color
		) {
			if(color == null) {
				ConfigCommand.setField(event.getGuild(), DatabaseField.COLOR, "");
				event.reply("Farbe zurückgesetzt").setEphemeral(true).queue();
				return;
			}


			try {
				Color.decode(color);

				ConfigCommand.setField(event.getGuild(), DatabaseField.COLOR, color);

				event.reply("Farbe geändert").setEphemeral(true).queue();
			} catch(NumberFormatException e) {
				event.reply("Ungültige Farbe").setEphemeral(true).queue();
			}
		}
	}

	@ApplicationCommand(name = "log_channel", description = "Ändert den Kanal für Logs")
	public static class LogChannelCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "kanal", description = "Der neue Log-Kanal", required = false) GuildMessageChannel channel
		) {
			if(channel == null) {
				ConfigCommand.setField(event.getGuild(), DatabaseField.LOG_CHANNEL, 0);
				event.reply("Log-Kanal zurückgesetzt").setEphemeral(true).queue();
				return;
			}


			if(!channel.getGuild().equals(event.getGuild())) {
				event.reply("Der Kanal ist nicht auf diesem Server!").setEphemeral(true).queue();
				return;
			}

			ConfigCommand.setField(event.getGuild(), DatabaseField.LOG_CHANNEL, channel.getIdLong());

			event.reply("Log-Kanal auf " + channel.getAsMention() + " gesetzt").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "greetings_channel", description = "Ändert den Kanal für Grüße")
	public static class GreetingsChannelCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "kanal", description = "Der neue Greetings-Kanal", required = false) GuildMessageChannel channel
		) {
			if(channel == null) {
				ConfigCommand.setField(event.getGuild(), DatabaseField.GREETINGS_CHANNEL, 0);
				event.reply("Greetings-Kanal zurückgesetzt").setEphemeral(true).queue();
				return;
			}


			if(!channel.getGuild().equals(event.getGuild())) {
				event.reply("Der Kanal ist nicht auf diesem Server!").setEphemeral(true).queue();
				return;
			}

			ConfigCommand.setField(event.getGuild(), DatabaseField.GREETINGS_CHANNEL, channel.getIdLong());

			event.reply("Greetings-Kanal auf " + channel.getAsMention() + " gesetzt").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "punishment_channel", description = "Ändert den Kanal für Straf-Logs")
	public static class PunishmentChannelCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "kanal", description = "Der neue Punishment-Kanal", required = false) GuildMessageChannel channel
		) {
			if(channel == null) {
				ConfigCommand.setField(event.getGuild(), DatabaseField.PUNISHMENT_CHANNEL, 0);
				event.reply("Punishment-Kanal zurückgesetzt").setEphemeral(true).queue();
				return;
			}

			if(!channel.getGuild().equals(event.getGuild())) {
				event.reply("Der Kanal ist nicht auf diesem Server!").setEphemeral(true).queue();
				return;
			}

			ConfigCommand.setField(event.getGuild(), DatabaseField.PUNISHMENT_CHANNEL, channel.getIdLong());

			event.reply("Punishment-Kanal auf " + channel.getAsMention() + " gesetzt").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "staff_role", description = "Ändert die Team-Rolle")
	public static class StaffRoleCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "rolle", description = "Die neue Staff-Rolle", required = false) Role role
		) {
			if(role == null) {
				ConfigCommand.setField(event.getGuild(), DatabaseField.STAFF_ROLE, 0);
				event.reply("Team-Rolle zurückgesetzt").setEphemeral(true).queue();
				return;
			}


			ConfigCommand.setField(event.getGuild(), DatabaseField.STAFF_ROLE, role.getIdLong());

			event.reply("Team-Rolle auf " + role.getAsMention() + " gesetzt").setEphemeral(true).queue();
		}
	}
}
