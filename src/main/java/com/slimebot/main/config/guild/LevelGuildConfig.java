package com.slimebot.main.config.guild;

import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.ConfigFieldType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Map;
import java.util.Optional;

public class LevelGuildConfig {
    @ConfigField(type = ConfigFieldType.CHANNEL, command = "notification_channel", title = "Notification Channel", description = "In diesem Channel wird für das neue Level Gratuliert")
    public Long notificationChannel;

    public Map<Integer, Long> levelRoles;

    public Optional<GuildMessageChannel> getChannel() {
        return GuildConfig.getChannel(notificationChannel);
    }
}
