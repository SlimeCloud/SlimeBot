package de.slimecloud.slimeball.features.highlights.commands;

import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.slimecloud.slimeball.features.highlights.HighlightListener;
import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "highlight", description = "Verwaltet Highlights", scope = Scope.GUILD_GLOBAL)
public class HighlightCommand {
	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull DiscordUtils<?> discordUtils, @NotNull Command<ICommandContext> command) {
		discordUtils.getJDA().addEventListener(new HighlightListener(bot));

		command.addSubcommand(HighlightListCommand.class);
		command.addSubcommand(HighlightAddCommand.class);
		command.addSubcommand(HighlightDeleteCommand.class);
	}
}
