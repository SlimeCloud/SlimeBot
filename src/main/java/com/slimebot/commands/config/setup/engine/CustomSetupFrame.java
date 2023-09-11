package com.slimebot.commands.config.setup.engine;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MessageFrame;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Optional;
import java.util.function.Function;

@Getter
public abstract class CustomSetupFrame extends MessageFrame {
    private final String name;

    protected CustomSetupFrame(String name, Menu menu, long guild, String title, String description, Function<GuildConfig, Optional<String>> value) {
        super(menu, () -> new EmbedBuilder()
                .setTitle(title)
                .setColor(GuildConfig.getColor(guild))
                .setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
                .setDescription(description)
                .addField("Aktueller Wert", value.apply(GuildConfig.getConfig(guild)).orElse("*Kein Wert*"), false)
                .build()
        );

        this.name = name;
    }
}
