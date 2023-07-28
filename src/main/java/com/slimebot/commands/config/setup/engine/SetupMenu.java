package com.slimebot.commands.config.setup.engine;

import com.slimebot.commands.config.engine.ConfigCategory;
import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.InstanceProvider;
import de.mineking.discord.ui.CallbackState;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.UIManager;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.FrameButton;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;

public class SetupMenu extends Menu {
	private final long guild;

	public SetupMenu(UIManager manager, String id, long guild) {
		super(manager, id);
		this.guild = guild;
	}

	public SetupMenu addMainFrame(List<ComponentRow> components) {
		addFrame("main", new SetupMainFrame(this, guild, components));
		return this;
	}

	public FrameButton addCategoryFrames(ConfigCategory category, Field[] fields, InstanceProvider instanceProvider) {
		FrameButton button = null;

		List<Field> configFields = Stream.of(fields)
				.filter(f -> f.isAnnotationPresent(ConfigField.class))
				.filter(f -> !f.getAnnotation(ConfigField.class).title().isEmpty())
				.toList();

		for(int i = 0; i < configFields.size(); i++) {
			Field field = configFields.get(i);
			ConfigField info = field.getAnnotation(ConfigField.class);

			String name = category.name() + " " + field.getName();

			addFrame(name, new ConfigFieldFrame(this, guild, category, field, info, instanceProvider,
					name,
					i == 0 ? "main" : category.name() + " " + configFields.get(i - 1).getName(),
					i == configFields.size() - 1 ? null : category.name() + " " + configFields.get(i + 1).getName())
			);

			if(button == null) {
				button = new FrameButton(ButtonColor.GRAY, category.description(), name);
			}
		}

		for(Class<? extends CustomSetupFrame> type : category.customFrames()) {
			try {
				CustomSetupFrame instance = type.getConstructor(Menu.class, long.class).newInstance(this, guild);

				addFrame(instance.name, instance);

				if(button == null) {
					button = new FrameButton(ButtonColor.GRAY, category.description(), instance.name);
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				logger.error("Failed to initialize " + type.getName(), e);
			}
		}

		if(button == null) {
			button = new FrameButton(ButtonColor.GRAY, category.description(), "main");
		}

		return button;
	}

	public void start(IReplyCallback event) {
		start(new CallbackState(event), "main");
	}
}
