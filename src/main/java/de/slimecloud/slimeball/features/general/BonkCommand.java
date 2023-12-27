package de.slimecloud.slimeball.features.general;


import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.SlimeEmoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "bonk", description = "Bonke eine Person", scope = Scope.GUILD_GLOBAL)
public class BonkCommand {

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "Wen willst du bonken?") User user
	) {
		event.reply(event.getUser().getAsMention() + " --> " + SlimeEmoji.BONK.toString(event.getGuild()) + " <-- " + user.getAsMention()).queue();
	}
}
