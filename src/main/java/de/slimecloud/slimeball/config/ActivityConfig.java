package de.slimecloud.slimeball.config;

import de.slimecloud.slimeball.config.engine.Required;
import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ActivityConfig {
	@Required
	public Integer interval;

	@Required
	public List<ActivityEntry> activities;

	public record ActivityEntry(@NotNull Activity.ActivityType type, @NotNull String text) {
		public Activity build() {
			return Activity.of(type, text);
		}
	}
}
