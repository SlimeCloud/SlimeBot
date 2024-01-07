package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.StringUtil;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
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
		update(event.getGuild());
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		update(event.getGuild());
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		update(event.getGuild());
	}

	@Override
	public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
		GuildConfig config = bot.loadGuild(event.getGuild());
		config.getStatistic().ifPresent(c -> c.setMemberCountChannel(null));
		config.save();
	}

	@Override
	public void onRoleDelete(@NotNull RoleDeleteEvent event) {
		GuildConfig config = bot.loadGuild(event.getGuild());
		config.getStatistic().ifPresent(c -> c.setMemberCountChannel(null));
		config.save();
	}

	public void update(@NotNull Guild guild) {
		bot.loadGuild(guild).getStatistic().ifPresent(c -> update(c, guild));
	}

	public void update(@NotNull StatisticConfig config, @NotNull Guild guild) {
		VoiceChannel channel = Optional.ofNullable(config.getMemberCountChannel()).map(bot.getJda()::getVoiceChannelById).orElse(null);
		if(channel == null) return;

		channel.getManager().setName(StringUtil.format(config.getMemberCountFormat(), Map.of("members", guild
				.getMembers()
				.stream()
				.filter(m -> !m.getUser().isBot())
				.count()
		))).queue();
	}
}
