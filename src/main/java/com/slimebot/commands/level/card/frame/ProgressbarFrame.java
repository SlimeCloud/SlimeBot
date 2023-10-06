package com.slimebot.commands.level.card.frame;

import com.slimebot.commands.level.card.CardComponent;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.FrameButton;

import java.util.Collection;
import java.util.List;

public class ProgressbarFrame extends CardFrame {
	public ProgressbarFrame(Menu menu) {
		super(menu, CardComponent.Part.PROGRESSBAR, "Hier kannst du deine Progressbar bearbeiten");
	}

	@Override
	public Collection<ComponentRow> getComponents() {
		return List.of(ComponentRow.of(
				COMPONENTS.BACK(),
				COMPONENTS.STYLE(this),
				COMPONENTS.BORDER(this),
				new FrameButton(ButtonColor.GRAY, "Farbe", "progressbar.color")
		));
	}
}
