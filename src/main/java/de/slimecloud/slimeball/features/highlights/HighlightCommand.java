package de.slimecloud.slimeball.features.highlights;

import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.list.ListManager;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "highlight", description = "Verwaltet Highlights", scope = Scope.GUILD_GLOBAL)
public class HighlightCommand {
	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull DiscordUtils<?> discordUtils, @NotNull ListManager<ICommandContext> list, @NotNull Command<ICommandContext> command) {
		discordUtils.getJDA().addEventListener(new HighlightListener(bot));

		command.addSubcommand(list.createCommand(state -> bot.getHighlights()).withDescription("Zeigt deine Highlights für diesen Server an"));
	}

	@ApplicationCommand(name = "add", description = "Füge ein Highlight auf diesem Server hinzu")
	public static class HighlightAddCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @NotNull @Option(minLength = 3, maxLength = 100) String phrase) {
			phrase = phrase.strip().toLowerCase();

			bot.getHighlights().set(event.getMember(), phrase);

			event.reply(String.format("Highlight `%s` erfolgreich hinzugefügt", phrase)).queue();
		}
	}

	@ApplicationCommand(name = "delete", description = "entferne ein Highlight auf diesem Server")
	public static class HighlightDeleteCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @NotNull @Option(minLength = 3, maxLength = 100) String phrase) {
			phrase = phrase.strip().toLowerCase();

			Highlight highlight = bot.getHighlights().remove(event.getMember(), phrase);

			if (highlight == null) event.reply("Highlight `%s` wurde nicht gefunden.".formatted(phrase)).queue();
			else event.reply("Highlight `%s` wurde erfolgreich entfernt.".formatted(phrase)).queue();
		}
	}
}
