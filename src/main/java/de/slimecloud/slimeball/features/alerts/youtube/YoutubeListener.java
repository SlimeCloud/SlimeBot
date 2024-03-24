package de.slimecloud.slimeball.features.alerts.youtube;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.Listener;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;

import java.util.Optional;

@Slf4j
@Listener
@RequiredArgsConstructor
public class YoutubeListener {
	private final SlimeBot bot;

	@EventHandler
	public void onUpload(YoutubeVideoEvent event) {
		logger.info("Video Uploaded: " + event.getVideo());
		bot.getJda().getGuilds().forEach(g -> bot.loadGuild(g).getYoutube().ifPresent(c -> {
			c.getChannel().ifPresentOrElse(channel -> {
				Optional<Role> role = c.getRole();
				String msg = event.isLive() ? c.getLiveMessage() : c.getVideoMessage();

				channel.sendMessage(msg
						.replace("%role%", role.map(IMentionable::getAsMention).orElse(""))
						.replace("%uploader%", event.getVideo().getChannel().getTitle())
						.replace("%url%", event.getVideo().getUrl())
				).queue();
			}, () -> logger.warn("Cannot send Youtube Notification because channel %s not found".formatted(c.getChannelId())));
		}));
	}
}
