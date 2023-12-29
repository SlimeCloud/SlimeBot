package de.slimecloud.slimeball.config;

import de.slimecloud.slimeball.main.SlimeBot;
import lombok.ToString;

@ToString
public class ConfigCategory {
	@ToString.Exclude
	public transient SlimeBot bot;

	public void enable() {
	}

	public void disable() {
	}

	public void update() {
	}
}
