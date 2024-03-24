package de.slimecloud.slimeball.features.alerts.youtube;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.Listener;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Slf4j
@Listener
@RequiredArgsConstructor
public class YoutubeListener {

	private final SlimeBot bot;

	@EventHandler
	public void onUpload(YoutubeVideoEvent event) {
		bot.getJda().getGuilds().forEach(g -> bot.loadGuild(g).getYoutube().ifPresent(c -> {
			TextChannel channel = bot.getJda().getTextChannelById(c.getChannel());
			if (channel != null) {
				String msg = event.isLive() ? c.getLiveMessage() : c.getVideoMessage();
				msg = msg.replace("%role%", String.format("<@&%s>", c.getRole()));
				msg = msg.replace("%url%", event.getVideo().getUrl());
				channel.sendMessage(msg).queue();
			} else logger.warn("Cannot send Youtube Notification because channel %s not found".formatted(c.getChannel()));
		}));
	}
}
