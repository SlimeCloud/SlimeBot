package de.slimecloud.slimeball.config.commands;

import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.ui.MessageMenu;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.engine.CategoryInfo;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

public class CategoryCommand extends Command<ICommandContext> {
	public CategoryCommand(@NotNull SlimeBot bot, @NotNull CommandManager<ICommandContext, ?> manager, @Nullable Field field, @NotNull Function<GuildConfig, Object> instance, @NotNull CategoryInfo category, @NotNull Field[] fields, @NotNull MessageMenu menu) {
		super(manager, category.command(), category.description());

		Map<String, Field> fieldMap = new HashMap<>();
		Set<Field> required = new HashSet<>();
		Set<Field> optional = new HashSet<>();

		for (Field f : fields) {
			//Ignore fields without @ConfigField
			ConfigField info = f.getAnnotation(ConfigField.class);
			if (info == null) continue;

			f.setAccessible(true);

			fieldMap.put(info.command(), f);

			if (info.required()) required.add(f);
			else optional.add(f);

			if (info.required()) continue;

			//Add subcommand for collections
			if (Collection.class.isAssignableFrom(f.getType())) addSubcommand(new ConfigCollectionCommand(bot, manager, instance, f, category, info, menu));
			else if (Map.class.isAssignableFrom(f.getType())) addSubcommand(new ConfigMapCommand(bot, manager, instance, f, category, info, menu));

				//Add subcommand for normal fields
			else addSubcommand(new ConfigFieldCommand(bot, manager, instance, f, category, info, menu));
		}

		//Add enable / disable
		if (field != null) {
			addSubcommand(new DisableCommand(bot, manager, field, category));
			addSubcommand(new EnableCommand(bot, manager, instance, field, category, fieldMap, required, optional));
		}
	}

	@NotNull
	public static CategoryCommand getCommand(@NotNull SlimeBot bot, @NotNull CommandManager<ICommandContext, ?> manager, @NotNull Field field, @NotNull MessageMenu menu) {
		CategoryInfo info = field.getAnnotation(CategoryInfo.class);
		if (info == null) throw new IllegalArgumentException("Category is missing annotation!");

		return new CategoryCommand(bot, manager, field, c -> {
			try {
				//Get category
				Object temp = field.get(c);

				//Create category if not present
				if (temp == null) {
					temp = field.getType().getConstructor().newInstance();
					field.set(c, temp);

					bot.updateGuildCommands(c.getGuild());
				}

				return temp;
			} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}, info, field.getType().getDeclaredFields(), menu);
	}

	@Override
	public void performCommand(@NotNull ICommandContext context) throws Exception {
	}
}
