package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.types.AtomicString;
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
import java.util.function.Function;


public class MemberCount extends ListenerAdapter {

	private final SlimeBot bot;
	private final Function<StatisticConfig, Long> channel;
	private final Function<StatisticConfig, String> format;

	public MemberCount(SlimeBot bot) {
		this.bot = bot;
		this.channel = StatisticConfig::getMemberCountChannel;
		this.format = StatisticConfig::getMemberCountFormat;
	}

	@Nullable
	private StatisticConfig getConfig(long guild) {
		return bot.loadGuild(guild).getStatistic().orElse(null);
	}

	@Nullable
	private VoiceChannel getChannel(long guild) {
		StatisticConfig config = getConfig(guild);
		if (config==null) return null;
		return bot.getJda().getVoiceChannelById(channel.apply(config));
	}

	@Nullable
	private String getFormat(long guild) {
		StatisticConfig config = getConfig(guild);
		if (config==null) return null;
		return format.apply(config);
	}

	@SuppressWarnings("ConstantConditions")
	private void update(long guild, @NotNull Map<String, Object> values) {
		VoiceChannel channel = getChannel(guild);
		if (channel==null) return;
		AtomicString format = new AtomicString(getFormat(guild));
		if (format.isEmpty()) return;
		values.forEach((k, v) -> format.set(format.get().replace("%" + k + "%", String.valueOf(v))));
		channel.getManager().setName(format.get()).queue();
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
