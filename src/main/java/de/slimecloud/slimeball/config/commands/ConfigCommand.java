package de.slimecloud.slimeball.config.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.ui.UIManager;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.engine.CategoryInfo;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@ApplicationCommand(name = "config", description = "Verwaltet die konfiguration des Servers", scope = Scope.GUILD_GLOBAL)
public class ConfigCommand {
	public CommandPermission permission = CommandPermission.TEAM;

	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull UIManager uiManager, @NotNull CommandManager<ICommandContext, ?> cmdManager, @NotNull Command<ICommandContext> command) {
		command.addSubcommand(MenuCommand.class);

		Set<Field> fields = new HashSet<>();

		for (Field f : GuildConfig.class.getDeclaredFields()) {
			f.setAccessible(true);

			//Add categories
			if (f.isAnnotationPresent(CategoryInfo.class)) command.addSubcommand(CategoryCommand.getCommand(bot, cmdManager, f));
			else if (f.isAnnotationPresent(ConfigField.class)) fields.add(f);
		}

		//Create general category
		command.addSubcommand(new CategoryCommand(bot, cmdManager, null, c -> c, GuildConfig.class.getAnnotation(CategoryInfo.class), fields.toArray(Field[]::new)));
	}
}
