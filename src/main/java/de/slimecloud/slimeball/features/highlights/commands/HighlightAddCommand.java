package de.slimecloud.slimeball.features.highlights.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "add", description = "Füge ein Highlight auf diesem Server hinzu", defer = true)
public class HighlightAddCommand {
	@ApplicationCommandMethod
	@SuppressWarnings("ConstantConditions")
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @NotNull @Option(minLength = 3, maxLength = 100) String phrase) {
		phrase = phrase.strip().toLowerCase();

		bot.getHighlights().set(event.getMember(), phrase);

		event.getHook().editOriginal(String.format("Highlight `%s` erfolgreich hinzugefügt", phrase)).queue();
	}
}
