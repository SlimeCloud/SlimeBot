package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MemberCount extends StatisticChannel {
	public MemberCount(SlimeBot bot) {
		super(bot, StatisticConfig::getMemberCountChannel, StatisticConfig::getMemberCountFormat);
	}

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event) {
		update(event);
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		update(event);
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		update(event);
	}

	private void update(@NotNull GenericGuildEvent event) {
		Guild guild = event.getGuild();
		update(guild.getIdLong(), Map.of("members", guild.getMembers().size()));
	}
}
