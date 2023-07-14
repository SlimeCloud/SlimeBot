package com.slimebot.commands.report;

import com.slimebot.report.Report;
import com.slimebot.report.Status;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "details", description = "Zeigt Details zu einer Meldung an")
public class DetailsCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(name = "id", description = "ID der Meldung") int id
	) {
		Report report = Report.get(event.getGuild().getId(), id);

		if(report == null) {
			event.reply("Report mit id " + id + " nicht gefunden!").setEphemeral(true).queue();
			return;
		}

		MessageEmbed embed = report.asEmbed(event.getGuild().getId());

		if(report.status == Status.CLOSED) {
			event.replyEmbeds(embed).setEphemeral(true).queue();
		}
		else {
			event.replyEmbeds(embed).setActionRow(Report.closeButton(report.id)).setEphemeral(true).queue();
		}
	}
}
