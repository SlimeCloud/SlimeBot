package de.slimecloud.slimeball.features.alerts.youtube;

import com.google.gson.JsonObject;
import de.cyklon.jevent.Event;
import de.slimecloud.slimeball.features.alerts.youtube.model.Video;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public class YoutubeApiErrorEvent extends Event {

	private final Response response;
	private final JsonObject jsonResponse;
	private final Set<Video> videos;

	public int getCode() {
		return response.code();
	}

}
