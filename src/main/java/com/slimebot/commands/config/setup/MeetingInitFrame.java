package com.slimebot.commands.config.setup;

import com.slimebot.commands.config.setup.engine.CustomSetupFrame;
import com.slimebot.events.MeetingListener;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import de.mineking.discord.ui.components.button.FrameButton;

import java.util.Optional;

public class MeetingInitFrame extends CustomSetupFrame {
	protected MeetingInitFrame(Menu menu, long guild) {
		super("meeting-init", menu, guild,
				"",
				"",
				config -> Optional.of("")
		);

		addComponents(ComponentRow.of(
				new ButtonComponent("init", ButtonColor.GREEN, "Initiale Nachricht senden").handle((m, event) -> {
					m.display("main");
					MeetingListener.sendEmptyMessage(event.getGuild());
				}),
				new FrameButton(ButtonColor.GRAY, "Überspringen", "main")
		));
	}
}
