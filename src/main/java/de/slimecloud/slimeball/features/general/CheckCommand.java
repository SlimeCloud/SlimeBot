package de.slimecloud.slimeball.features.general;


import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "check", description = "check check??", scope = Scope.GUILD_GLOBAL)
public class CheckCommand {

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event) {
		event.reply("*Ihre Nachricht lautet: HEIL MAIS!*").queue();
	}
}
