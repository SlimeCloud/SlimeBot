package com.slimebot.commands.level.card.frame;

import com.slimebot.commands.level.card.CardComponent;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MenuBase;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import de.mineking.discord.ui.components.button.FrameButton;

import java.util.Collection;
import java.util.List;

public class MainFrame extends CardFrame {

    public MainFrame(Menu menu) {
        super(menu, null, "Hier kannst du deine RankCard bearbeiten");
    }

    @Override
    public Collection<ComponentRow> getComponents(CardComponent COMPONENTS) {
        return List.of(ComponentRow.of(
                new GroupButton("Avatar", "avatar"),
                new GroupButton("Hintergrund", "background"),
                new GroupButton("Progressbar", "progressbar"),
                new GroupButton("Reset", "reset"),
                new ButtonComponent("close", ButtonColor.RED, "✖ Schließen").addHandler(m -> m.close())
        ));
    }

    private static class GroupButton extends FrameButton {

        public GroupButton(String label, String menu) {
            super(ButtonColor.GRAY, label, menu);
            prependHandler(MenuBase::setLoading);
        }
    }

}
