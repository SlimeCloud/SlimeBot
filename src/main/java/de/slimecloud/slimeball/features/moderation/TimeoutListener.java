package de.slimecloud.slimeball.features.moderation;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.Listener;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Optional;

@Listener
@RequiredArgsConstructor
public class TimeoutListener extends ListenerAdapter {
	private final SlimeBot bot;

	@Override
	public void onGuildMemberUpdateTimeOut(@NotNull GuildMemberUpdateTimeOutEvent event) {
		if (event.getNewTimeOutEnd() == null) return;

		//Discord doesn't provide the team member to use, so we fetch it from the audit logs
		event.getGuild().retrieveAuditLogs().order(PaginationAction.PaginationOrder.BACKWARD).limit(10).forEachAsync(entry -> {
			if (entry.getType() != ActionType.AUTO_MODERATION_MEMBER_TIMEOUT && entry.getType() != ActionType.MEMBER_UPDATE) return true;
			if (entry.getTargetIdLong() != event.getUser().getIdLong()) return true; //Continue non-matches

			if (bot.getConfig().getTimeoutIgnore().contains(entry.getUserIdLong())) return false;

			String reason = Optional.ofNullable(entry.getReason()).orElse("N/A");

			//Call event and remove timout if canceled
			if (new UserTimeoutedEvent(event.getMember(), entry.getUser(), reason, event.getNewTimeOutEnd().toInstant()).callEvent())
				event.getMember().removeTimeout().queue();

			return false;
		});
	}

	@EventHandler
	public void onTimeout(@NotNull UserTimeoutedEvent event) {
		String team = event.getTeam() != null ? event.getTeam().getAsMention() : "Automod";

		event.getTarget().getUser().openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(new EmbedBuilder()
				.setTitle("Du wurdest getimeouted")
				.setColor(bot.getColor(event.getTarget().getGuild()))
				.setTimestamp(Instant.now())
				.setDescription("Du wurdest auf dem SlimeCloud Discord getimeouted")
				.addField("Teammitglied", team, true)
				.addField("Endet", TimeFormat.RELATIVE.format(event.getEnd()), true)
				.addField("Grund", event.getReason(), false)
				.build()
		)).mapToResult().flatMap(res -> bot.loadGuild(event.getTarget().getGuild()).getPunishmentChannel().map(channel -> (RestAction<?>) channel.sendMessageEmbeds(new EmbedBuilder()
				.setTitle("\uD83D\uDE34  **" + event.getTarget().getEffectiveName() + "** wurde getimeouted")
				.setColor(bot.getColor(event.getTarget().getGuild()))
				.setTimestamp(Instant.now())
				.addField("Nutzer", event.getTarget().getAsMention(), true)
				.addField("Teammitglied", team, true)
				.addField("Endet", TimeFormat.RELATIVE.format(event.getEnd()), true)
				.addField("Nutzer Informiert", res.isSuccess() ? "Ja" : "Nein", true)
				.addField("Grund", event.getReason(), false)
				.build()
		)).orElse(bot.wrap(null))).queue();
	}
}
