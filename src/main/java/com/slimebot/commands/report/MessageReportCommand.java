package com.slimebot.commands.report;

import com.slimebot.main.Main;
import com.slimebot.report.Report;
import com.slimebot.report.Type;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.time.Instant;

@ApplicationCommand(name = "Nachricht melden", type = Command.Type.MESSAGE, guildOnly = true)
public class MessageReportCommand {
	@ApplicationCommandMethod
	public void performCommand(MessageContextInteractionEvent event) {
		if(Main.blocklist(event.getGuild().getId()).contains(event.getMember().getId())) {
			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.embedColor(event.getGuild().getId()))
							.setTitle(":exclamation: Error: Blocked")
							.setDescription("Du wurdest gesperrt, so dass du keine Reports mehr erstellen kannst")
							.build()
			).setEphemeral(true).queue();
			return;
		}

		Report.createReport(event, id -> new Report(id, Type.MSG, event.getTarget().getAuthor(), event.getUser(), event.getTarget().getJumpUrl()), event.getTarget().getAuthor());
	}
}
