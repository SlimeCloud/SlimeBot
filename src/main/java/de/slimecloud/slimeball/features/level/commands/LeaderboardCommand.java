package de.slimecloud.slimeball.features.level.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.commands.option.defaultValue.IntegerDefault;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.features.level.Level;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationCommand(name = "leaderboard", description = "Zeigt das aktuelle Leaderboard", scope = Scope.GUILD, defer = true)
public class LeaderboardCommand {
	public final static int MAX_NAME_LENGTH = 13;
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getLevel().isPresent();

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @IntegerDefault(10) @Option(description = "Maximale Anzahl am Mitgliedern, die angezeigt werden", minValue = 3, maxValue = 15, required = false) int max,
	                           @IntegerDefault(0) @Option(description = "Der Rang, bei dem begonnen werden soll", minValue = 0, required = false) int offset

	) {
		//Load leaderboard
		List<Level> levels = bot.getLevel().getTopList(event.getGuild(), offset, max);

		//Check for valid leaderboard
		if (levels.isEmpty()) {
			event.getHook().editOriginal("Für die angegebenen Parameter konnte kein Leaderboard gefunden werden!").queue();
			return;
		}

		//Find highest level
		int highest = levels.stream()
				.mapToInt(Level::getLevel)
				.max().orElseThrow(); //This can't happen because we already caught the empty case above

		//Send result
		event.getHook().editOriginalEmbeds(new EmbedBuilder()
				.setTitle("\uD83D\uDCDD  Leaderboard")
				.setColor(bot.getColor(event.getGuild()))
				.setDescription(
						"```ansi\n" +
								levels.stream()
										.map(l -> "\033[1m" + StringUtil.padRight(StringUtils.abbreviate(event.getGuild().getMember(l.getUser()).getEffectiveName(), MAX_NAME_LENGTH), MAX_NAME_LENGTH) + "\033[0m" +
												"[" +
												StringUtil.createProgressBar((double) l.getLevel() / highest, 30) +
												"] \033[34;1m(" + l.getLevel() + ")\033[0m"
										)
										.collect(Collectors.joining("\n")) +
								"```"
				)
				.build()
		).queue();
	}
}
