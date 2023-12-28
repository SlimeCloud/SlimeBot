package de.slimecloud.slimeball.features.report;

import de.cyklon.jevent.EventHandler;
import de.slimecloud.slimeball.events.ReportCreateEvent;
import de.slimecloud.slimeball.events.UserReportedEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class ReportListener {
	private final SlimeBot bot;

	public ReportListener(@NotNull SlimeBot bot) {
		this.bot = bot;
	}

	@EventHandler
	public void handleReport(@NotNull UserReportedEvent event) {
		//Cancel if reporter is blocked
		bot.getReportBlocks().isBlocked(event.getReport().getIssuer(), event.getReport().getGuild()).ifPresent(block -> {
			event.getEvent().replyEmbeds(
					new EmbedBuilder()
							.setTitle("\uD83D\uDC6E Blockiert")
							.setColor(bot.getColor(event.getReport().getGuild()))
							.setDescription("Du wurdest gesperrt, so dass du keine Reports mehr erstellen kannst")
							.addField("Grund", block.getReason(), true)
							.setTimestamp(Instant.now())
							.build()
			).setEphemeral(true).queue();
			event.setCancelled(true); //Cancel report
		});
	}

	@EventHandler
	public void logReport(@NotNull ReportCreateEvent event) {
		//Send log for team members
		bot.loadGuild(event.getReport().getGuild()).getPunishmentChannel()
				.ifPresent(channel -> channel.sendMessage(event.getReport().buildMessage("Neuer Report")).queue());
	}
}
