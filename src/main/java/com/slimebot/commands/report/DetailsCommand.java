package com.slimebot.commands.report;

import com.slimebot.report.Report;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.commands.annotated.option.Option;
import de.mineking.discord.events.interaction.SelectionHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

@ApplicationCommand(name = "details", description = "Zeigt Details zu einer Meldung an")
public class DetailsCommand {
	@WhenFinished
	public void setup(DiscordUtils manager) {
		manager.getEventManager().registerHandler(new SelectionHandler<>(StringSelectInteractionEvent.class, "report:details", event -> {
			event.editComponents(event.getMessage().getComponents()).queue(); //Remove selection

			Report.get(event.getGuild(), Integer.parseInt(event.getValues().get(0)))
					.ifPresent(report -> event.getHook().sendMessage(report.buildMessage()).setEphemeral(true).queue());
		}));
	}

	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(name = "id", description = "ID der Meldung") int id
	) {
		Report.get(event.getGuild(), id)
				.ifPresentOrElse(
						report -> event.reply(report.buildMessage()).setEphemeral(true).queue(),
						() -> event.reply("Report nicht gefunden").setEphemeral(true).queue()
				);
	}
}
