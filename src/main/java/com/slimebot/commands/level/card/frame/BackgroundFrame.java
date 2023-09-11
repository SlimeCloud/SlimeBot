package com.slimebot.commands.level.card.frame;

import com.slimebot.commands.level.card.CardComponent;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.FrameButton;

import java.util.Collection;
import java.util.List;

public class BackgroundFrame extends CardFrame {

	public BackgroundFrame(Menu menu) {
		super(menu, CardComponent.Part.BACKGROUND, "Hier kannst du deinen Hintergrund bearbeiten");
	}

	@Override
	public Collection<ComponentRow> getComponents(CardComponent COMPONENTS) {
		return List.of(ComponentRow.of(COMPONENTS.BACK(), COMPONENTS.BORDER(this), new FrameButton(ButtonColor.GRAY, "Hintergrund", "background.modal")));
	}
}
