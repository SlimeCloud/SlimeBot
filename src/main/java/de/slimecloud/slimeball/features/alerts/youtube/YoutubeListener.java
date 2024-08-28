package de.slimecloud.slimeball.features.alerts.youtube;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.Listener;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.IMentionable;

@Slf4j
@Listener
@RequiredArgsConstructor
public class YoutubeListener {
	private final SlimeBot bot;

	@EventHandler
	public void onUpload(YoutubeVideoEvent event) {
		logger.info("Video Uploaded: {}", event.getVideo());
		bot.getJda().getGuilds().forEach(g -> bot.loadGuild(g).getYoutube().ifPresent(config ->
				config.getChannel(event.getChannelId()).ifPresentOrElse(channel -> {
					String msg = event.isLive() ? config.getLiveMessage().get(event.getChannelId()) : config.getVideoMessage().get(event.getChannelId());

					channel.sendMessage(msg
							.replace("%role%", config.getRole(event.getChannelId()).map(IMentionable::getAsMention).orElse(""))
							.replace("%uploader%", event.getVideo().getChannel().getTitle())
							.replace("%url%", event.getVideo().getUrl())
							.replace("%title%", event.getVideo().snippet().title())
					).queue();
				}, () -> logger.warn("Cannot send Youtube Notification because channel {} not found", config.getChannelId(event.getChannelId()).orElse(null)))
		));
	}

	@EventHandler
	public void onApiError(YoutubeApiErrorEvent event) {

		logger.warn("Youtube API error {} '{}'", event.getCode(), event.getJsonResponse().getAsJsonObject("error").get("message").getAsString());
	}
}
