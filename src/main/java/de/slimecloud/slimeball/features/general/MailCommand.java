package de.slimecloud.slimeball.features.general;


import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "mail", description = "neue Mail!", scope = Scope.GUILD_GLOBAL)
public class MailCommand {

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event) {
		event.reply("*Sie haben eine neue Nachricht. Tippen Sie **!check**, um sie abzuhören*").queue();
	}
}
