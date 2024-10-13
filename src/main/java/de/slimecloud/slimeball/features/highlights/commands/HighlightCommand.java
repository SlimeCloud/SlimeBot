package de.slimecloud.slimeball.features.highlights.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.list.ListManager;
import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "highlights", description = "Verwaltet Highlights", scope = Scope.GUILD)
public class HighlightCommand {
	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull Command<ICommandContext> command, @NotNull ListManager<ICommandContext> manager) {
		command.addSubcommand(HighlightListCommand.class);
		command.addSubcommand(HighlightAddCommand.class);
		command.addSubcommand(HighlightDeleteCommand.class);
	}
}
