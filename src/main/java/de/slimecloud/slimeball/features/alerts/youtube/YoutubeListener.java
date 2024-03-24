package de.slimecloud.slimeball.features.alerts.youtube;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.Listener;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Slf4j
@Listener
@RequiredArgsConstructor
public class YoutubeListener {
	private final SlimeBot bot;

	@EventHandler
	public void onUpload(YoutubeVideoEvent event) {
		bot.getJda().getGuilds().forEach(g -> bot.loadGuild(g).getYoutube().ifPresent(c -> {
			TextChannel channel = c.getChannel();

			if (channel != null) {
				Role role = c.getRole();
				String msg = event.isLive() ? c.getLiveMessage() : c.getVideoMessage();

				channel.sendMessage(msg
						.replace("%role%", role == null ? "" : role.getAsMention())
						.replace("%uploader%", event.getVideo().getChannel().getTitle())
						.replace("%url%", event.getVideo().getUrl())
				).queue();
			} else logger.warn("Cannot send Youtube Notification because channel %s not found".formatted(c.getChannel()));
		}));
	}
}
