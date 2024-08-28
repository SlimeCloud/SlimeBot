package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.list.ListManager;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "birthday", description = "Verwaltet Geburtstage", scope = Scope.GUILD)
public class BirthdayCommand {
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getBirthday().isPresent();

	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull Command<ICommandContext> command, @NotNull ListManager<ICommandContext> manager) {
		command.addSubcommand(BirthdayInfoCommand.class);
		command.addSubcommand(RememberBirthdayCommand.class);
		command.addSubcommand(ForgetBirthdayCommand.class);

		command.addSubcommand(manager.createCommand(s -> bot.getBirthdays()).withName("next").withDescription("Zeigt die nächsten Mitglieder an die Geburtstag haben"));
	}
}
