package com.slimebot.commands.config.engine;

import com.slimebot.main.CommandContext;
import de.mineking.discord.commands.inherited.BaseCommand;

import java.lang.reflect.Field;

public class ConfigCategoryCommand extends BaseCommand<CommandContext> {
    public ConfigCategoryCommand(ConfigCategory category, Field[] fields, InstanceProvider instanceProvider) {
        description = category.description();

        for (Field field : fields) {
            ConfigField info = field.getAnnotation(ConfigField.class);

            if (info == null) continue;

            if (info.type() == ConfigFieldType.CHANNEL_LIST || info.type() == ConfigFieldType.ROLE_LIST || info.type() == ConfigFieldType.NUMBER_LIST || info.type() == ConfigFieldType.STRING_LIST) {
                addSubcommand(info.command() + "_add", new ConfigArrayPropertyCommand(field, info, category, instanceProvider, ConfigArrayPropertyCommandType.ADD));
                addSubcommand(info.command() + "_remove", new ConfigArrayPropertyCommand(field, info, category, instanceProvider, ConfigArrayPropertyCommandType.REMOVE));
                return;
            }

            addSubcommand(info.command(), new ConfigPropertyCommand(field, info, category, instanceProvider));
        }
    }
}
