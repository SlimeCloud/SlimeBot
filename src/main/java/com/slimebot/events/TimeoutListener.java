package com.slimebot.events;

import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;

public class TimeoutListener extends ListenerAdapter {

	@Override
	public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent event) {
		if(!event.getMember().isTimedOut()) return;

		for(AuditLogEntry entry : event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE)) {
			if(entry.getTargetId().equals(event.getMember().getId())) {
				event.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(
						new EmbedBuilder()
								.setTitle("Du wurdest getimeouted")
								.setColor(Main.database.getColor(event.getGuild()))
								.setTimestamp(Instant.now())
								.setDescription("Du wurdest auf dem SlimeCloud Discord getimeouted")
								.addField("Grund:", entry.getReason(), true)
								.build()
				)).queue();

				Main.database.getChannel(event.getGuild(), DatabaseField.PUNISHMENT_CHANNEL)
						.sendMessageEmbeds(
								new EmbedBuilder()
										.setTitle("\"" + event.getMember().getEffectiveName() + "\"" + " wurde getimeouted")
										.setColor(Main.database.getColor(event.getGuild()))
										.setTimestamp(Instant.now())
										.addField("Grund:", entry.getReason(), true)
										.addField("Wer: ", event.getMember().getAsMention(), true)
										.build()
						).queue();

				break;
			}
		}
	}
}
