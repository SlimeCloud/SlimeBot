package com.slimebot.commands.config.setup;

import com.slimebot.commands.config.setup.engine.CustomSetupFrame;
import com.slimebot.events.MeetingListener;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import de.mineking.discord.ui.components.button.FrameButton;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MeetingInitFrame extends CustomSetupFrame {
	public MeetingInitFrame(Menu menu) {
		super("meeting init", menu,
				"Meeting Initialisieren",
				"Um das erste Meeting zu initialisieren, drücke den Knopf unter der Nachricht. Dadurch wird eine Nachricht gesendet, über die das heutige und die folgenden Meetings kontrolliert werden können"
		);
	}

	@Override
	public Optional<String> getValue(GuildConfig config) {
		return null;
	}

	@Override
	public Collection<ComponentRow> getComponents() {
		return List.of(
				ComponentRow.of(
						new ButtonComponent("init", ButtonColor.GREEN, "Initiale Nachricht senden").addHandler((m, event) -> {
							m.display("main");
							MeetingListener.sendEmptyMessage(event.getGuild());
						}),
						new FrameButton(ButtonColor.GRAY, "Überspringen", "main")
				)
		);
	}
}
