package com.slimebot.events;


import com.slimebot.alerts.spotify.SpotifyListener;
import com.slimebot.main.BuildInfo;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.message.StaffMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class ReadyListener extends ListenerAdapter {
	@Override
	public void onReady(@NotNull net.dv8tion.jda.api.events.session.ReadyEvent event) {
		try {
			if (Main.spotify != null) {
				Main.spotify.register();
			}

			Main.holiday.run();
		} catch (Exception e) {
			SpotifyListener.getLogger().error("Konnte spotify listener nicht starten", e);
		}

		Main.discordUtils.getCommandCache().updateGlobalCommands(error -> Main.getLogger().error("Failed to update commands", error));
	}

	@Override
	public void onGuildJoin(@NotNull GuildJoinEvent event) {
		Main.updateGuildCommands(event.getGuild());
	}

	@Override
	public void onGuildReady(GuildReadyEvent event) {
		GuildConfig.load(event.getGuild());
		Main.updateGuildCommands(event.getGuild());

		StaffMessage.updateMessage(event.getGuild());

		GuildConfig.getConfig(event.getGuild()).getLogChannel().ifPresent(channel ->
				channel.sendMessageEmbeds(
						new EmbedBuilder()
								.setTitle("Bot wurde gestartet")
								.setDescription("Der Bot hat sich mit der DiscordAPI (neu-) verbunden")
								.addField("Version:", BuildInfo.version, true)
								.addField("Serveranzahl:", String.valueOf(Main.jdaInstance.getGuilds().size()), true)
								.setColor(GuildConfig.getColor(event.getGuild()))
								.setTimestamp(Instant.now())
								.build()
				).queue()
		);
	}
}
