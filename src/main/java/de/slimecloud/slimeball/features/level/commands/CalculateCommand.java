package de.slimecloud.slimeball.features.level.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.commands.option.defaultValue.IntegerDefault;
import de.slimecloud.slimeball.features.level.LevelTable;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationCommand(name = "calculate", description = "Berechnet die benötigten XP für ein Level")
public class CalculateCommand {
	public final CommandPermission permission = null;

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(minValue = 1, description = "Das Level, das berechnet werden soll") int level,
	                           @IntegerDefault(1) @Option(minValue = 1, maxValue = 50, required = false, description = "Die Anzahl an Leveln") Integer limit
	) {
		event.replyEmbeds(new EmbedBuilder()
				.setColor(bot.getColor(event.getGuild()))
				.setTitle("Benötigte XP für Level")
				.setThumbnail(event.getGuild().getIconUrl())
				.setDescription(IntStream.range(level, level + limit)
						.mapToObj(l -> "- **Level " + l + "** benötigt **" + LevelTable.getRequiredXp(l) + " XP**")
						.collect(Collectors.joining("\n"))
				)
				.build()
		).setEphemeral(true).queue();
	}
}
