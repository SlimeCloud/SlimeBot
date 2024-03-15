package de.slimecloud.slimeball.features.general;


import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "cornhub", description = "Socials von Miká und Nico", scope = Scope.GUILD_GLOBAL)
public class CornhubCommand {

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event) {
		event.reply("Hier kommt ihr zu Nicos (https://open.spotify.com/artist/0ZzsW7JiW4Ok3H7nFl4yV1) und Mikás (https://www.youtube.com/@gamingguidesde) Cornhub!").queue();
	}
}
