package de.slimecloud.slimeball.features.report.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@ApplicationCommand(name = "Nachricht melden", type = Command.Type.MESSAGE, scope = Scope.GUILD_GLOBAL)
public class MessageReportCommand {
	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull MessageContextInteractionEvent event) {
		//Try report and check for succeessful insert
		if (!bot.getReports().reportMessage(event, event.getTarget())) return;

		//Send confirmation
		event.replyEmbeds(new EmbedBuilder()
				.setTitle(":white_check_mark: Report Erfolgreich")
				.setColor(bot.getColor(event.getGuild()))
				.setDescription(event.getTarget().getAuthor().getAsMention() + " wurde erfolgreich gemeldet")
				.setTimestamp(Instant.now())
				.build()
		).setEphemeral(true).queue();
	}
}
