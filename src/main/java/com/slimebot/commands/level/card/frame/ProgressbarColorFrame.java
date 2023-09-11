package com.slimebot.commands.level.card.frame;

import com.slimebot.database.DataClass;
import com.slimebot.level.profile.CardProfile;
import com.slimebot.util.ColorUtil;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MenuBase;
import de.mineking.discord.ui.ModalFrameBase;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.awt.*;
import java.util.Map;
import java.util.function.Supplier;

public class ProgressbarColorFrame extends ModalFrameBase {

    private CardProfile profile;

    public ProgressbarColorFrame(Menu menu) {
        super(menu);
    }

    @Override
    public void setup() {
        Supplier<CardProfile> sup = () -> new CardProfile(menu.getGuild().getIdLong(), menu.getMember().getIdLong());
        profile = DataClass.load(sup, Map.of("guild", menu.getGuild().getIdLong(), "user", menu.getMember().getIdLong())).orElseGet(sup);
    }

    @Override
    public Modal getModal(String id) {
        return Modal.create(id, "Progressbar Farbe")
                .addActionRow(TextInput.create("color", "Farbe", TextInputStyle.SHORT)
                        .setRequired(false)
                        .setMinLength(3)
                        .setMaxLength(15)
                        .setPlaceholder("#46eb34")
                        .build())
                .addActionRow(TextInput.create("background.color", "Hintergrund Farbe", TextInputStyle.SHORT)
                        .setRequired(false)
                        .setMinLength(3)
                        .setMaxLength(15)
                        .setPlaceholder("#46eb34")
                        .build())
                .build();
    }

    @Override
    public void handle(MenuBase menu, ModalInteractionEvent event) {
        menu.setLoading();
        ModalMapping color = event.getValue("color");
        ModalMapping backgroundColor = event.getValue("background.color");
        boolean flag = false;
        if (color != null) {
            Color c = ColorUtil.parseColor(color.getAsString());
            if (c != null) {
                profile.setProgressBarColor(c.getRGB());
                flag = true;
            }
        }
        if (backgroundColor != null) {
            Color c = ColorUtil.parseColor(backgroundColor.getAsString());
            if (c != null) {
                profile.setProgressBarBGColor(c.getRGB());
                flag = true;
            }
        }
        if (flag) profile.save();
        menu.display("progressbar");
    }
}
