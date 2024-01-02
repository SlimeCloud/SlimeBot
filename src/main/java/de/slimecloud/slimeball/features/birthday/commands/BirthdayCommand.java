package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.Scope;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "birthday", description = "Hiermit kannst du geburtstage ansehen, eintragen...", scope = Scope.GUILD_GLOBAL)
public class BirthdayCommand {

	@Setup
	public static void setup(@NotNull Command<?> command) {
		command.addSubcommand(BirthdayInfoCommand.class);
		command.addSubcommand(RememberBirthdayCommand.class);
		command.addSubcommand(ForgetBirthdayCommand.class);
	}

}
