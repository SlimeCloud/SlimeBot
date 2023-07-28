package com.slimebot.commands.config.engine;

import com.slimebot.main.CommandContext;
import de.mineking.discord.commands.inherited.BaseCommand;

import java.lang.reflect.Field;

public class ConfigCategoryCommand extends BaseCommand<CommandContext> {
	public ConfigCategoryCommand(ConfigCategory category, Field[] fields, InstanceProvider instanceProvider) {
		description = category.description();

		for(Field field : fields) {
			ConfigField info = field.getAnnotation(ConfigField.class);

			if(info == null) continue;

			addSubcommand(info.command(), new ConfigPropertyCommand(field, info, category, instanceProvider));
		}
	}
}
