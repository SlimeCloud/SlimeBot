package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "forget-birthday", description = "Lösche dein Geburtstag", scope = Scope.GUILD_GLOBAL, defer = true)
public class ForgetBirthdayCommand {

	@ApplicationCommandMethod
	@SuppressWarnings("ConstantConditions")
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
		bot.getBirthdayTable().remove(event.getMember());
		event.getHook().editOriginal(":white_check_mark: Dein geburtstag wurde gelöscht!").queue();
	}

}
