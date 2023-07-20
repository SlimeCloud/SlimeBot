package com.slimebot.commands.config;

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
		                           @Option(name = "farbe", description = "Der HEX-Code für die Farbe", required = false) String color
		) {
			if(color == null) {
				ConfigCommand.updateField(event.getGuild(), config -> config.color = null);
				event.reply("Farbe zurückgesetzt").setEphemeral(true).queue();
				return;
			}

			try {
				Color.decode(color); //Verify

				ConfigCommand.updateField(event.getGuild(), config -> config.color = color);

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
				ConfigCommand.updateField(event.getGuild(), config -> config.logChannel = null);
				event.reply("Log-Kanal zurückgesetzt").setEphemeral(true).queue();
				return;
			}

			if(!channel.getGuild().equals(event.getGuild())) {
				event.reply("Der Kanal ist nicht auf diesem Server!").setEphemeral(true).queue();
				return;
			}

			ConfigCommand.updateField(event.getGuild(), config -> config.logChannel = channel.getIdLong());

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
				ConfigCommand.updateField(event.getGuild(), config -> config.greetingsChannel = null);
				event.reply("Greetings-Kanal zurückgesetzt").setEphemeral(true).queue();
				return;
			}

			if(!channel.getGuild().equals(event.getGuild())) {
				event.reply("Der Kanal ist nicht auf diesem Server!").setEphemeral(true).queue();
				return;
			}

			ConfigCommand.updateField(event.getGuild(), config -> config.greetingsChannel = channel.getIdLong());

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
				ConfigCommand.updateField(event.getGuild(), config -> config.punishmentChannel = null);
				event.reply("Punishment-Kanal zurückgesetzt").setEphemeral(true).queue();
				return;
			}

			if(!channel.getGuild().equals(event.getGuild())) {
				event.reply("Der Kanal ist nicht auf diesem Server!").setEphemeral(true).queue();
				return;
			}

			ConfigCommand.updateField(event.getGuild(), config -> config.punishmentChannel = channel.getIdLong());

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
				ConfigCommand.updateField(event.getGuild(), config -> config.staffRole = null);
				event.reply("Team-Rolle zurückgesetzt").setEphemeral(true).queue();
				return;
			}


			ConfigCommand.updateField(event.getGuild(), config -> config.staffRole = role.getIdLong());

			event.reply("Team-Rolle auf " + role.getAsMention() + " gesetzt").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "contributor_role", description = "Ändert die Contributor-Rolle")
	public static class ContributorRoleCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "rolle", description = "Die neue Contributor-Rolle", required = false) Role role
		) {
			if(role == null) {
				ConfigCommand.updateField(event.getGuild(), config -> config.contributorRole = null);
				event.reply("Contributor-Rolle zurückgesetzt").setEphemeral(true).queue();
				return;
			}

			ConfigCommand.updateField(event.getGuild(), config -> config.contributorRole = role.getIdLong());

			event.reply("Contributor-Rolle auf " + role.getAsMention() + " gesetzt").setEphemeral(true).queue();
		}
	}
}
