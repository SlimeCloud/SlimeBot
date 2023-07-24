package com.slimebot.commands.report;

import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "report_user", description = "Melde einen Nutzer", guildOnly = true)
public class UserReportSlashCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(name = "nutzer") User user,
	                           @Option(name = "begründung") String reason
	) {
		UserReportCommand.submitUserReport(event, user, reason);
	}
}
