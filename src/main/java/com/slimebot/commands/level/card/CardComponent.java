package com.slimebot.commands.level.card;

import com.slimebot.commands.level.card.frame.CardFrame;
import com.slimebot.database.DataClass;
import com.slimebot.level.profile.CardProfile;
import com.slimebot.level.profile.Style;
import de.mineking.discord.ui.MenuBase;
import de.mineking.discord.ui.components.Component;
import de.mineking.discord.ui.components.button.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.Map;
import java.util.function.Supplier;

public class CardComponent {

    public enum Part {
        AVATAR,
        BACKGROUND,
        PROGRESSBAR
    }

    private final CardProfile profile;

    private CardComponent(CardProfile profile) {
        this.profile = profile;
    }

    private void save() {
        profile.save();
        //TODO update image
    }

    public Component<ButtonInteractionEvent> STYLE(CardFrame frame) {
        return STYLE(frame.part);
    }

    public Component<ButtonInteractionEvent> STYLE(Part part) {
        return new ToggleButton(String.format("%s:%s:style", profile.getUser(), part.name().toLowerCase()), new ToggleHolder() {
            @Override
            public void setState(boolean state, MenuBase menu, ButtonInteractionEvent event) {
                menu.setLoading();
                boolean flag = true;
                switch (part) {
                    case AVATAR -> profile.setAvatarStyle(Style.fromState(state));
                    case PROGRESSBAR -> profile.setProgressBarStyle(Style.fromState(state));
                    default -> flag = false;
                }
                if (flag) save();
            }

            @Override
            public boolean getState(MenuBase menu) {
                return part == Part.AVATAR ? profile.getAvatarStyle().asState() : (part == Part.PROGRESSBAR && profile.getProgressBarStyle().asState());
            }
        }, ToggleButton.blueGreen, s -> new ButtonLabel(s ? "Rund" : "Eckig"));
    }

    public Component<ButtonInteractionEvent> BACK() {
        return new FrameButton(ButtonColor.GRAY, "Zurück", "main").prependHandler(MenuBase::setLoading);
    }

    public Component<ButtonInteractionEvent> BORDER(CardFrame frame) {
        return BORDER(frame.part);
    }

    public Component<ButtonInteractionEvent> BORDER(Part part) {
        return new FrameButton(ButtonColor.GRAY, "Border", "border").prependHandler(menu -> menu.putData("part", part));
    }


    public static CardComponent fromMember(Member member) {
        Supplier<CardProfile> sup = () -> new CardProfile(member.getGuild().getIdLong(), member.getIdLong());
        return new CardComponent(DataClass.load(sup, Map.of("guild", member.getGuild().getIdLong(), "user", member.getIdLong())).orElseGet(sup));
    }

}
