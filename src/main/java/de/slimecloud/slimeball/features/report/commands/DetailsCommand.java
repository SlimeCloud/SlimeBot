package de.slimecloud.slimeball.features.report.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "details", description = "Zeigt Details zu einer Meldung an")
public class DetailsCommand {
	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "ID der Meldung") int id
	) {
		bot.getReports().getReport(id).ifPresentOrElse(
				report -> event.reply(report.buildMessage("Details zu Report")).setEphemeral(true).queue(),
				() -> event.reply("Report nicht gefunden").setEphemeral(true).queue()
		);
	}
}
