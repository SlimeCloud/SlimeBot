package com.slimebot.commands.config.setup.engine;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MessageFrame;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

public class SetupMainFrame extends MessageFrame {
    public SetupMainFrame(Menu menu, long guild, List<ComponentRow> components) {
        super(menu, () -> new EmbedBuilder()
                .setTitle("\uD83D\uDD27 Einstellungs-Menü")
                .setColor(GuildConfig.getColor(guild))
                .setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
                .setDescription("In diesem Menü kann die Konfiguration des Bots für diesen Server eingestellt werden. Nutze die Knöpfe um ein Untermenü zu öffnen, in dem du dann Einstellungen für die Entsprechende Funktion vornehmen " +
                        "kannst")
                .build()
        );

        addComponents(components);
        addComponents(new ButtonComponent("close", ButtonColor.RED, "Menü Schließen").addHandler((m, evt) -> m.close()));
    }
}
