package com.slimebot.report.contextmenus;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Type;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;

public class MsgReport extends ListenerAdapter {

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		if(!event.getFullCommandName().equals("Report Message")) return;

		String msg = event.getTarget().getContentRaw();

		if(msg.length() > 800) {
			msg = msg.substring(0, 800) + "...";
		}

		String msgWithLink = "[" + msg + "](" + event.getTarget().getJumpUrl() + ")";

		Report report = Report.createReport(event.getGuild(), Type.MESSAGE, event.getUser(), event.getTarget().getAuthor(), msgWithLink);

		event.replyEmbeds(
				new EmbedBuilder()
						.setTimestamp(Instant.now())
						.setColor(Main.database.getColor(event.getGuild()))
						.setTitle(":white_check_mark: Report Erfolgreich")
						.setDescription(event.getTarget().getAuthor().getAsMention() + " wurde erfolgreich gemeldet")
						.build()
		).setEphemeral(true).queue();

		report.log();
	}
}


