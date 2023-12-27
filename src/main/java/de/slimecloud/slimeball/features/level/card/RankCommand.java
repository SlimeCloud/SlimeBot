package de.slimecloud.slimeball.features.level.card;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "rank", description = "Zeigt Level-Informationen zu einer Nutzer an", scope = Scope.GUILD)
public class RankCommand {
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getLevel().isPresent();

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(name = "target", description = "Der Nutzer, dessen Rank angezeigt werden soll", required = false) Member target
	) {
		event.deferReply().queue();

		event.getHook().editOriginalAttachments(
				bot.getLevelProfiles().getProfile(target != null ? target : event.getMember())
						.render()
						.getFile()
		).queue();
	}
}
