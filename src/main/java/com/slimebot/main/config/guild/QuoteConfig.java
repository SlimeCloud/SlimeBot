package com.slimebot.main.config.guild;

import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.ConfigFieldType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Optional;

public class QuoteConfig {
    @ConfigField(type = ConfigFieldType.CHANNEL, command = "quote_channel", title = "Zitate Kanal", description = "In diesem Kanal werden Zitate gesendet")
    public Long quoteChannel;

    public Optional<GuildMessageChannel> getChannel() {
        return GuildConfig.getChannel(quoteChannel);
    }
}
