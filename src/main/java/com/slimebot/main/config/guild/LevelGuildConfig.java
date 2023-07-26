package com.slimebot.main.config.guild;

import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.ConfigFieldType;

public class LevelGuildConfig {

    @ConfigField(type = ConfigFieldType.CHANNEL, command = "notification_channel", title = "Notification Channel", description = "In diesem Channel wird für das neue Level Gratuliert")
    public Long notificationChannel;

}
