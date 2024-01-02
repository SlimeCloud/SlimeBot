package de.slimecloud.slimeball.config.commands;

import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.engine.CategoryInfo;
import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class DisableCommand extends Command<ICommandContext> {
	private final SlimeBot bot;
	private final Field field;
	private final CategoryInfo category;

	public DisableCommand(@NotNull SlimeBot bot, @NotNull CommandManager<ICommandContext, ?> manager, @NotNull Field field, @NotNull CategoryInfo category) {
		super(manager, "disable", "Deaktiviert dieses Feature");

		this.bot = bot;
		this.field = field;
		this.category = category;
	}

	@Override
	public void performCommand(@NotNull ICommandContext context) throws Exception {
		//Load config and disable category
		GuildConfig config = bot.loadGuild(context.getEvent().getGuild());

		//Get current to call disable method
		Object current = field.get(config);
		if (current instanceof ConfigCategory c) c.disable(context.getEvent().getGuild());

		field.set(config, null);
		config.save();

		//Update commands to remove commands that might be affected by this
		bot.updateGuildCommands(context.getEvent().getGuild());

		context.getEvent().reply("✅ Feature **" + category.name() + "** deaktiviert").setEphemeral(true).queue();
	}
}
