package com.slimebot.main.config.guild;

import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.ConfigFieldType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Optional;

public class FdmdsConfig {
    @ConfigField(type = ConfigFieldType.CHANNEL, command = "channel", title = "Fdmds Kanal", description = "In diesen Kanal werden die Fdmds fragen gesendet")
    public Long channel;
    @ConfigField(type = ConfigFieldType.CHANNEL, command = "log_channel", title = "Fdmds Log Kanal", description = "In diesen Kanal werden Fdmds Nachrichten zur Verifikation gesendet")
    public Long logChannel;

    @ConfigField(type = ConfigFieldType.ROLE, command = "notification_role", title = "Fdmds Rolle", description = "Die Rolle, die bei neuen Fdmds Fragen erwähnt wird")
    public Long role;

    public Optional<GuildMessageChannel> getChannel() {
        return GuildConfig.getChannel(channel);
    }

    public Optional<GuildMessageChannel> getLogChannel() {
        return GuildConfig.getChannel(logChannel);
    }

    public Optional<Role> getRole() {
        return GuildConfig.getRole(role);
    }
}
