package com.slimebot.commands.report;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
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
		if(BlockCommand.isBlocked(event.getMember())) {
			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(GuildConfig.getColor(event.getGuild()))
							.setTitle(":exclamation: Error: Blocked")
							.setDescription("Du wurdest gesperrt, so dass du keine Reports mehr erstellen kannst")
							.build()
			).setEphemeral(true).queue();
			return;
		}

		String messageContent = event.getTarget().getContentRaw();

		if(messageContent.length() > 800) {
			messageContent = messageContent.substring(0, 800) + "...";
		}

		Report report = Report.createReport(event.getGuild(), Type.MESSAGE, event.getUser(), event.getTarget().getAuthor(), "[" + messageContent + "](" + event.getTarget().getJumpUrl() + ")");

		event.replyEmbeds(
				new EmbedBuilder()
						.setTimestamp(Instant.now())
						.setColor(GuildConfig.getColor(event.getGuild()))
						.setTitle(":white_check_mark: Report Erfolgreich")
						.setDescription(event.getTarget().getAuthor().getAsMention() + " wurde erfolgreich gemeldet")
						.build()
		).setEphemeral(true).queue();

		report.log();
	}
}
