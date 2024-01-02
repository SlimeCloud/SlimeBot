package de.slimecloud.slimeball.features.level.commands;

import de.cyklon.jevent.JEvent;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.features.level.LevelListener;
import de.slimecloud.slimeball.features.level.LevelUpListener;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "level", description = "Verwaltet die Level eines Nutzers", scope = Scope.GUILD)
public class LevelCommand {
	public final CommandPermission permission = CommandPermission.TEAM;
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getLevel().isPresent();

	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull Command<?> command) {
		bot.getJda().addEventListener(new LevelListener(bot));
		JEvent.getDefaultManager().registerListener(new LevelUpListener(bot));

		command.addSubcommand(AddCommand.class);
		command.addSubcommand(RemoveCommand.class);
		command.addSubcommand(SetCommand.class);
	}
}
