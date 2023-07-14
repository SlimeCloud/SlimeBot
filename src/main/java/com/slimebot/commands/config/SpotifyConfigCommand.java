package com.slimebot.commands.config;

import com.slimebot.main.DatabaseField;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "spotify", description = "Ändert die Konfiguration zu Spotify Alerts")
public class SpotifyConfigCommand {
	@ApplicationCommand(name = "music_channel", description = "Ändert den Kanal für Music Alerts")
	public static class MusicChannelCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "kanal", description = "Der neue Music-Alert-Kanal", required = false) GuildMessageChannel channel
		) {
			if(channel == null) {
				ConfigCommand.setField(event.getGuild(), DatabaseField.SPOTIFY_MUSIC_CHANNEL, 0);
				event.reply("Music-Kanal zurückgesetzt").setEphemeral(true).queue();
				return;
			}

			if(!channel.getGuild().equals(event.getGuild())) {
				event.reply("Der Kanal ist nicht auf diesem Server!").setEphemeral(true).queue();
				return;
			}

			ConfigCommand.setField(event.getGuild(), DatabaseField.SPOTIFY_MUSIC_CHANNEL, channel.getIdLong());

			event.reply("Music-Alert-Kanal auf " + channel.getAsMention() + " gesetzt").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "podcast_channel", description = "Ändert den Kanal für Podcast alerts")
	public static class PodcastChannelCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "kanal", description = "Der neue Podcast-Alert-Kanal", required = false) GuildMessageChannel channel
		) {
			if(channel == null) {
				ConfigCommand.setField(event.getGuild(), DatabaseField.SPOTIFY_PODCAST_CHANNEL, 0);
				event.reply("Podcast-Kanal zurückgesetzt").setEphemeral(true).queue();
				return;
			}

			if(!channel.getGuild().equals(event.getGuild())) {
				event.reply("Der Kanal ist nicht auf diesem Server!").setEphemeral(true).queue();
				return;
			}

			ConfigCommand.setField(event.getGuild(), DatabaseField.SPOTIFY_PODCAST_CHANNEL, channel.getIdLong());

			event.reply("Podcast-Alert-Kanal auf " + channel.getAsMention() + " gesetzt").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "notification_role", description = "Ändert die Spotify-Notification-Rolle")
	public static class NotificationRoleCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "rolle", description = "Die neue Spotify-Notification-Rolle", required = false) Role role
		) {
			if(role == null) {
				ConfigCommand.setField(event.getGuild(), DatabaseField.SPOTIFY_NOTIFICATION_ROLE, 0);
				event.reply("Spotify-Notification-Rolle zurückgesetzt").setEphemeral(true).queue();
				return;
			}

			ConfigCommand.setField(event.getGuild(), DatabaseField.SPOTIFY_NOTIFICATION_ROLE, role.getIdLong());

			event.reply("Spotify-Notification-Rolle auf " + role.getAsMention() + " gesetzt").setEphemeral(true).queue();
		}
	}
}
