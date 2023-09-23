package com.slimebot.events;

import com.slimebot.main.CommandPermission;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.AutoDeleteConfig;
import com.slimebot.main.config.guild.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AutoDeleteListener extends ListenerAdapter {
	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (shouldDelete(event.getMessage(), event.getChannel())) event.getMessage()
				.delete()
				.flatMap(x -> deleteFeedback(event.getAuthor(), event.getChannel().asGuildMessageChannel()))
				.queue();
	}

	@Override
	public void onChannelCreate(ChannelCreateEvent event) {
		if (!(event.getChannel() instanceof ThreadChannel thread)) return;

		buildThreadDelete(thread)
				.onErrorFlatMap(e -> buildThreadDelete(thread).delay(5, TimeUnit.SECONDS))
				.flatMap(x -> deleteFeedback(thread.getOwner().getUser(), thread.getParentChannel()))
				.queueAfter(1, TimeUnit.SECONDS);
	}

	private RestAction<Void> buildThreadDelete(ThreadChannel thread) {
		return thread.retrieveStartMessage()
				.flatMap(mes -> shouldDelete(mes, thread.getParentChannel())
						? thread.delete()
						: new CompletedRestAction<>(Main.jdaInstance, null)
				);
	}

	private RestAction<?> deleteFeedback(User user, GuildChannel channel) {
		return user.openPrivateChannel()
				.flatMap(ch -> ch.sendMessageEmbeds(
						new EmbedBuilder()
								.setTitle("Nachricht gelöscht")
								.setColor(GuildConfig.getColor(channel.getGuild()))
								.setDescription("Deine Nachricht in " + channel.getAsMention() + " wurde gelöscht, da sie nicht den Filtern für den Kanal entspricht")
								.addField(
										"Folgende Nachrichten werden akzeptiert",
										GuildConfig.getConfig(channel.getGuild()).getAutoDeleteConfig()
												.map(a -> a.autoDeleteChannels.get(channel.getId()))
												.map(f -> "```" +
														f.stream()
																.map(AutoDeleteConfig.Filter::getName)
																.collect(Collectors.joining("\n")) +
														"```"
												)
												.orElse("*Keine*"),
										false
								)
								.build()
				))
				.mapToResult();
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