package com.slimebot.events;

import com.slimebot.main.config.guild.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;
import java.util.Optional;

public class TimeoutListener extends ListenerAdapter {

	@Override
	public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent event) {
		if(!event.getMember().isTimedOut()) return;

		for(AuditLogEntry entry : event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE)) {
			if(entry.getTargetId().equals(event.getMember().getId())) {
				String reason = Optional.ofNullable(entry.getReason()).orElse("N/A");

				event.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(
						new EmbedBuilder()
								.setTitle("Du wurdest getimeouted")
								.setColor(GuildConfig.getColor(event.getGuild()))
								.setTimestamp(Instant.now())
								.setDescription("Du wurdest auf dem SlimeCloud Discord getimeouted")
								.addField("Grund:", reason, true)
								.addField("Teammitglied:", entry.getUser().getAsMention(), true)
								.build()
				)).queue();

				GuildConfig.getConfig(entry.getGuild()).getPunishmentChannel().ifPresent(channel ->
						channel.sendMessageEmbeds(
								new EmbedBuilder()
										.setTitle("\"" + event.getMember().getEffectiveName() + "\"" + " wurde getimeouted")
										.setColor(GuildConfig.getColor(event.getGuild()))
										.setTimestamp(Instant.now())
										.addField("Grund:", reason, true)
										.addField("Wer: ", event.getMember().getAsMention(), true)
										.addField("Teammitglied:", entry.getUser().getAsMention(), true)
										.build()
						).queue()
				);

				break;
			}
		}
	}
}
