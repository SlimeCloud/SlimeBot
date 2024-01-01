package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.types.AtomicString;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
public abstract class StatisticChannel extends ListenerAdapter {

	private final SlimeBot bot;
	private final Function<StatisticConfig, Long> channel;
	private final Function<StatisticConfig, String> format;

	@Nullable
	protected final StatisticConfig getConfig(long guild) {
		return bot.loadGuild(guild).getStatistic().orElse(null);
	}

	@Nullable
	protected final VoiceChannel getChannel(long guild) {
		StatisticConfig config = getConfig(guild);
		if (config==null) return null;
		return bot.getJda().getVoiceChannelById(channel.apply(config));
	}

	@Nullable
	protected final String getFormat(long guild) {
		StatisticConfig config = getConfig(guild);
		if (config==null) return null;
		return format.apply(config);
	}

	protected final void update(long guild, @NotNull Map<String, Object> values) {
		VoiceChannel channel = getChannel(guild);
		if (channel==null) return;
		AtomicString format = new AtomicString(getFormat(guild));
		if (format.get()==null) return;
		values.forEach((k, v) -> format.set(format.get().replace("%" + k + "%", String.valueOf(v))));
		channel.getManager().setName(format.get()).queue();
	}

}
