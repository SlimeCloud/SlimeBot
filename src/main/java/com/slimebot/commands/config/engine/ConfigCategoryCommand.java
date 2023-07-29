package com.slimebot.commands.config.engine;

import com.slimebot.main.CommandContext;
import de.mineking.discord.commands.inherited.BaseCommand;

import java.lang.reflect.Field;

public class ConfigCategoryCommand extends BaseCommand<CommandContext> {
	public ConfigCategoryCommand(ConfigCategory category, Field[] fields, InstanceProvider instanceProvider) {
		description = category.description();

		for(Field field : fields) {
			if(!field.isAnnotationPresent(ConfigField.class)) continue;

			ConfigField info = field.getAnnotation(ConfigField.class);
			addSubcommand(info.command(), new ConfigPropertyCommand(field, info, category, instanceProvider));
		}
	}
}
