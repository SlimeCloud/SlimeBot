package com.slimebot.commands.config.setup;

import com.slimebot.commands.config.engine.ConfigCategory;
import com.slimebot.commands.config.setup.engine.SetupMenu;
import com.slimebot.main.CommandPermission;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.FrameButton;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@ApplicationCommand(name = "setup", description = "Startet einen Setup-Wizard, mit dem eine initiale Konfiguration erstellt werden kann", guildOnly = true)
public class SetupCommand {
	public CommandPermission permission = CommandPermission.TEAM;

	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event) {
		showSetupMenu(event);
	}

	public static void showSetupMenu(IReplyCallback event) {
		SetupMenu menu = Main.discordUtils.getUIManager()
				.createMenu((manger, id) -> new SetupMenu(manger, id, event.getGuild().getIdLong()));

		List<FrameButton> buttons = new ArrayList<>();

		List<Field> mainFields = new ArrayList<>();

		for (Field field : GuildConfig.class.getFields()) {
			if (Modifier.isTransient(field.getModifiers())) continue;

			if (field.isAnnotationPresent(ConfigCategory.class)) {
				buttons.add(menu.addCategoryFrames(field.getAnnotation(ConfigCategory.class), field.getType().getFields(), (create, config) -> {
					try {
						Object temp = field.get(config);

						if (temp == null && create) {
							temp = field.getType().getConstructor().newInstance();
							field.set(config, temp);
						}

						return temp;
					} catch (IllegalAccessException | NoSuchMethodException | InstantiationException |
					         InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}));
			} else {
				mainFields.add(field);
			}
		}

		buttons.add(0, menu.addCategoryFrames(GuildConfig.class.getAnnotation(ConfigCategory.class), mainFields.toArray(Field[]::new), (create, config) -> config));

		AtomicInteger counter = new AtomicInteger();
		menu.addMainFrame(
				buttons.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / 5)).values().stream()
						.map(ComponentRow::of)
						.toList()
		).start(event);
	}
}
