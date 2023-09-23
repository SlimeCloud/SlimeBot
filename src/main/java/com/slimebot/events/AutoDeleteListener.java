package com.slimebot.events;

import com.slimebot.main.CommandPermission;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.AutoDeleteConfig;
import com.slimebot.main.config.guild.GuildConfig;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class AutoDeleteListener extends ListenerAdapter {
	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (shouldDelete(event.getMessage(), event.getChannel())) event.getMessage().delete().queue();
	}

	@Override
	public void onChannelCreate(ChannelCreateEvent event) {
		if (!(event.getChannel() instanceof ThreadChannel thread)) return;

		Main.executor.schedule(
				() -> thread.retrieveStartMessage().queue(mes -> {
					if (shouldDelete(mes, thread.getParentChannel())) thread.delete().queue();
				}),
				1, TimeUnit.SECONDS
		);
	}

	private boolean shouldDelete(Message message, Channel channel) {
		if (!message.isFromGuild() || message.isWebhookMessage()) return false;
		if (CommandPermission.TEAM.isPermitted(message.getMember())) return false;

		EnumSet<AutoDeleteConfig.Filter> filters = GuildConfig.getConfig(message.getGuild())
				.getAutoDeleteConfig().map(a -> a.autoDeleteChannels.get(channel.getId()))
				.orElse(null);

		if (filters == null) return false;

		Predicate<Message> filter = null;
		for (AutoDeleteConfig.Filter f : filters) {
			filter = filter == null ? f.getFilter() : filter.or(f.getFilter());
		}

		return filter == null || !filter.test(message);
	}
}
