package com.slimebot.events;

import com.slimebot.main.CommandPermission;
import com.slimebot.main.config.guild.AutoDeleteConfig;
import com.slimebot.main.config.guild.GuildConfig;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.function.Predicate;

public class AutoDeleteListener extends ListenerAdapter {
	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (!event.isFromGuild()) return;
		if (!CommandPermission.TEAM.isPermitted(event.getMember())) return;

		EnumSet<AutoDeleteConfig.Filter> filters = GuildConfig.getConfig(event.getGuild()).getAutoDeleteConfig().map(a -> a.autoDeleteChannels.get(event.getChannel().getId())).orElse(null);

		if (filters == null) return;

		Predicate<Message> filter = null;
		for (AutoDeleteConfig.Filter f : filters) {
			filter = filter == null ? f.getFilter() : filter.or(f.getFilter());
		}

		if (filter == null || !filter.test(event.getMessage())) event.getMessage().delete().queue();
	}
}
