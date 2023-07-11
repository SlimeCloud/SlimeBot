package com.slimebot.events;


import com.slimebot.alerts.spotify.SpotifyListener;
import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.util.Date;

public class StartupListener extends ListenerAdapter {
	@Override
	public void onReady(@NotNull net.dv8tion.jda.api.events.session.ReadyEvent event) {
		Main.discordUtils.getCommandCache().updateGlobalCommands(error -> Main.logger.error("Failed to update commands", error));

		try {
			Main.spotify.register();
		} catch(Exception e) {
			SpotifyListener.logger.error("Konnte spotify listener nicht starten", e);
		}

		for(Guild guild : Main.jdaInstance.getGuilds()) {
			Config.createMain(guild.getId());

			YamlFile config = Config.getConfig(guild.getId(), "mainConfig");
			YamlFile reportFile = Config.getConfig(guild.getId(), "reports");

			try {
				config.load();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}

			if(!(config.exists())) {
				Config.createMain(guild.getId());
			}

			else {
				MessageChannel channel = Main.jdaInstance.getGuildById(guild.getId()).getChannelById(MessageChannel.class, config.getString("logChannel"));

				if(channel == null) continue;

				EmbedBuilder embed = new EmbedBuilder()
						.setTitle("Bot wurde gestartet")
						.setDescription("Der Bot hat sich mit der DiscordAPI (neu-) verbunden")
						.setColor(Main.embedColor(guild.getId()))
						.setTimestamp(new Date().toInstant());

				channel.sendMessageEmbeds(embed.build()).queue();
			}

			if(!reportFile.exists()) {
				try {
					reportFile.createNewFile();
					reportFile.load();
					reportFile.set("reports.abc", "def");
					reportFile.save();
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
