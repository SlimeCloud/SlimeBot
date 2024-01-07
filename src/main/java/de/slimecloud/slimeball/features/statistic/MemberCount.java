package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.StringUtil;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class MemberCount extends ListenerAdapter {
	private final SlimeBot bot;

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
		update(event.getGuild());
	}

	public void update(@NotNull Guild guild) {
		bot.loadGuild(guild).getStatistic().ifPresent(config -> {
			VoiceChannel channel = Optional.ofNullable(config.getMemberCountChannel()).map(bot.getJda()::getVoiceChannelById).orElse(null);
			if(channel == null) return;

			channel.getManager().setName(StringUtil.format(config.getMemberCountFormat(), Map.of("members", guild
					.getMembers()
					.stream()
					.filter(m -> !m.getUser().isBot())
					.count()
			))).queue();
		});
	}
}
