package com.slimebot.main.config.guild;

import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.ConfigFieldType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.*;

public class LevelGuildConfig {
	@ConfigField(type = ConfigFieldType.CHANNEL, command = "notification_channel", title = "Notification Channel", description = "In diesem Channel wird für das neue Level Gratuliert")
	public Long notificationChannel;

	@ConfigField(type = ConfigFieldType.NUMBER, command = "multiplier", title = "XP-Multiplier", description = "Multiplier für XP", menu = false)
	public Double xpMultiplier;

	public Map<Integer, Long> levelRoles = new HashMap<>();
	public List<Long> blacklist = new ArrayList<>();

	public Optional<GuildMessageChannel> getChannel() {
		return GuildConfig.getChannel(notificationChannel);
	}
}
