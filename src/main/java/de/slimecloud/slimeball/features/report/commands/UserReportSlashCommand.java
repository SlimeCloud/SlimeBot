package de.slimecloud.slimeball.features.report.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "report_user", description = "Melde einen Nutzer", scope = Scope.GUILD_GLOBAL)
public class UserReportSlashCommand {
	private final SlimeBot bot;

	public UserReportSlashCommand(@NotNull SlimeBot bot) {
		this.bot = bot;
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "Der Nutzer, den du melden möchtest") User user,
	                           @Option(description = "Die Begründung für deine Meldung") String reason
	) {
		UserReportCommand.submitUserReport(bot, event, user, reason);
	}
}
