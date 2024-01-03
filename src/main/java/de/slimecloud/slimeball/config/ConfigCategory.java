package de.slimecloud.slimeball.config;

import de.slimecloud.slimeball.main.SlimeBot;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

@ToString
public class ConfigCategory {
	@ToString.Exclude
	public transient SlimeBot bot;

	public void enable(@NotNull Guild guild) {
	}

	public void disable(@NotNull Guild guild) {
	}

	public void update(@NotNull Guild guild) {
	}
}
