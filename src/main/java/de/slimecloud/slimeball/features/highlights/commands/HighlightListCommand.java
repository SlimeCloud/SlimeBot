package de.slimecloud.slimeball.features.highlights.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.context.IAutocompleteContext;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.slimecloud.slimeball.features.highlights.Highlight;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationCommand(name = "list", description = "zeigt deine Highlights auf diesem Server an", defer = true)
public class HighlightListCommand {
	@ApplicationCommandMethod
	@SuppressWarnings("ConstantConditions")
	public void performCommand(@NotNull CommandManager<ICommandContext, IAutocompleteContext> manager, @NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
		List<Highlight> highlights = bot.getHighlights().getHighlights(event.getMember());

		String description = "Deine Highlights:\n" + highlights.stream()
				.map(Highlight::getPhrase)
				.collect(Collectors.joining("\n"));

		String mention = manager.getCommand(HighlightAddCommand.class).getAsMention(event.getGuild().getIdLong());

		event.getHook().editOriginalEmbeds(new EmbedBuilder()
				.setColor(bot.getColor(event.getGuild()))
				.setTitle("Highlights")
				.setDescription(highlights.isEmpty() ? "Du hast noch keine Highlights hinzugefügt.\nMit %s kannst du highlights hinzufügen".formatted(mention) : description)
				.build()).queue();

	}
}
