package de.slimecloud.slimeball.features.highlights;

import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.commands.*;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.IAutocompleteContext;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.features.highlights.Highlight;
import de.slimecloud.slimeball.features.highlights.HighlightListener;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationCommand(name = "highlight", description = "Verwaltet Highlights", scope = Scope.GUILD_GLOBAL)
public class HighlightCommand {
	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull DiscordUtils<?> discordUtils, @NotNull Command<ICommandContext> command) {
		discordUtils.getJDA().addEventListener(new HighlightListener(bot));

		command.addSubcommand(HighlightListCommand.class);
		command.addSubcommand(HighlightAddCommand.class);
		command.addSubcommand(HighlightDeleteCommand.class);
	}


	@ApplicationCommand(name = "add", description = "Füge ein Highlight auf diesem Server hinzu", defer = true)
	public static class HighlightAddCommand {
		@ApplicationCommandMethod
		@SuppressWarnings("ConstantConditions")
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @NotNull @Option(minLength = 3, maxLength = 100) String phrase) {
			phrase = phrase.strip().toLowerCase();

			bot.getHighlights().set(event.getMember(), phrase);

			event.getHook().editOriginal(String.format("Highlight `%s` erfolgreich hinzugefügt", phrase)).queue();
		}
	}

	@ApplicationCommand(name = "delete", description = "entferne ein Highlight auf diesem Server", defer = true)
	public static class HighlightDeleteCommand {
		@ApplicationCommandMethod
		@SuppressWarnings("ConstantConditions")
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @NotNull @Option(minLength = 3, maxLength = 100) String phrase) {
			phrase = phrase.strip().toLowerCase();

			Highlight highlight = bot.getHighlights().remove(event.getMember(), phrase);

			if (highlight == null) event.getHook().editOriginal("Highlight `%s` wurde nicht gefunden.".formatted(phrase)).queue();
			else event.getHook().editOriginal("Highlight `%s` wurde erfolgreich entfernt.".formatted(phrase)).queue();
		}
	}

	@ApplicationCommand(name = "list", description = "zeigt deine Highlights auf diesem Server an", defer = true)
	public static class HighlightListCommand {
		@ApplicationCommandMethod
		@SuppressWarnings("ConstantConditions")
		public void performCommand(@NotNull CommandManager<ICommandContext, IAutocompleteContext> manager, @NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
			List<Highlight> highlights = bot.getHighlights().getHighlights(event.getMember());

			String description = "**Deine Highlights:**\n" + highlights.stream()
					.map(Highlight::getPhrase)
					.map(s -> "- `" + s + "`")
					.collect(Collectors.joining("\n"));

			String mention = manager.getCommand(HighlightAddCommand.class).getAsMention(event.getGuild().getIdLong());

			event.getHook().editOriginalEmbeds(new EmbedBuilder()
					.setColor(bot.getColor(event.getGuild()))
					.setTitle("Highlights")
					.setDescription(highlights.isEmpty() ? "Du hast noch keine Highlights hinzugefügt.\nMit %s kannst du highlights hinzufügen".formatted(mention) : description)
					.build()).queue();

		}
	}
}
