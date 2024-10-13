package de.slimecloud.slimeball.features.highlights.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.features.highlights.Highlight;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "delete", description = "entferne ein Highlight auf diesem Server", defer = true)
public class HighlightDeleteCommand {
	@ApplicationCommandMethod
	@SuppressWarnings("ConstantConditions")
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @NotNull @Option(minLength = 3, maxLength = 100) String phrase) {
		phrase = phrase.strip().toLowerCase();

		Highlight highlight = bot.getHighlights().remove(event.getMember(), phrase);

		if (highlight == null) event.getHook().editOriginal("Highlight `%s` wurde nicht gefunden.".formatted(phrase)).queue();
		else event.getHook().editOriginal("Highlight `%s` wurde erfolgreich entfernt.".formatted(phrase)).queue();
	}
}
