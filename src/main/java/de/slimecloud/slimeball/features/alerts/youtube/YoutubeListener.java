package de.slimecloud.slimeball.features.alerts.youtube;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.Listener;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.IMentionable;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Listener
public class YoutubeListener {

	@EventHandler
	public void onUpload(@NotNull SlimeBot bot, @NotNull YoutubeVideoEvent event) {
		logger.info("Video Uploaded: {}", event.getVideo());
		bot.getJda().getGuilds().forEach(g -> bot.loadGuild(g).getYoutube().ifPresent(config ->
				config.getChannel(event.getYoutubeChannelId()).ifPresentOrElse(channel -> {
					String msg = event.isLive() ? config.getLiveMessage().get(event.getYoutubeChannelId()) : config.getVideoMessage().get(event.getYoutubeChannelId());

					channel.sendMessage(msg
							.replace("%role%", config.getRole(event.getYoutubeChannelId()).map(IMentionable::getAsMention).orElse(""))
							.replace("%uploader%", event.getVideo().getChannel().getTitle())
							.replace("%url%", event.getVideo().getUrl())
							.replace("%title%", event.getVideo().snippet().title())
					).queue();
				}, () -> logger.warn("Cannot send Youtube Notification because channel {} not found", config.getChannelId(event.getYoutubeChannelId()).orElse(null)))
		));
	}

	@EventHandler
	public void onApiError(@NotNull YoutubeApiErrorEvent event) {
		logger.warn("Youtube API error {} '{}'", event.getCode(), event.getJsonResponse().getAsJsonObject("error").get("message").getAsString());
	}
}
