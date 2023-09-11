package com.slimebot.commands.level.card.frame;

import com.slimebot.commands.level.card.CardComponent;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.components.ComponentRow;

import java.util.Collection;
import java.util.List;

public class AvatarFrame extends CardFrame {

    public AvatarFrame(Menu menu) {
        super(menu, CardComponent.Part.AVATAR, "Hier kannst du dein Avatar bearbeiten");
    }

    @Override
    public Collection<ComponentRow> getComponents(CardComponent COMPONENTS) {
        return List.of(ComponentRow.of(COMPONENTS.BACK(), COMPONENTS.STYLE(this), COMPONENTS.BORDER(this)));
    }
}
