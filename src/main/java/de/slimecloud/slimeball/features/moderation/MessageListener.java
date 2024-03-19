package de.slimecloud.slimeball.features.moderation;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.JEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageListener extends ListenerAdapter {
	public static final Pattern URL_PATTERN = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.CASE_INSENSITIVE);
	private final SlimeBot bot;

	public MessageListener(@NotNull SlimeBot bot) {
		this.bot = bot;
		JEvent.getDefaultManager().registerListener(this);
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (!event.isFromGuild()) return;
		check(event.getMessage(), event.getMessage().getChannel(), false);
	}

	@Override
	public void onChannelCreate(@NotNull ChannelCreateEvent event) {
		if (!(event.getChannel() instanceof ThreadChannel thread)) return;
		if (!(thread.getParentChannel() instanceof IPostContainer)) return;

		thread.retrieveStartMessage().queue(
				message -> check(message, thread.getParentChannel(), true),
				new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE)
		);
	}

	private void check(@NotNull Message message, @NotNull Channel channel, boolean thread) {
		if (!message.isFromGuild()) return;
		if (message.isWebhookMessage()) return;

		bot.loadGuild(message.getGuild()).getAutodelete(channel).ifPresent(filters -> {
			if (filters.stream().anyMatch(f -> f.getFilter().test(message)) || new AutoDeleteFlagedEvent(thread, channel, message).callEvent()) {
				if (!message.isFromGuild() || !bot.loadGuild(message.getGuild()).isAutoThread(message.getChannel().getIdLong())) return;
				message.createThreadChannel(
						StringUtils.abbreviate(
								"(" + message.getAuthor().getEffectiveName() + ") " + getThreadName(message),
								ThreadChannel.MAX_NAME_LENGTH
						)
				).queue();
			}
		});
	}

	@EventHandler(priority = -1)
	public void delete(@NotNull AutoDeleteFlagedEvent event) {
		//Ignore bots
		if (event.getMessage().getAuthor().isBot()) return;

		//Ignore team members
		if (bot.loadGuild(event.getMessage().getGuild()).getTeamRole().map(event.getMessage().getMember().getRoles()::contains).orElse(false)) {
			event.setCancelled(true);
			return;
		}

		if (event.isThread()) event.getMessage().getChannel().delete().queue();
		else event.getMessage().delete().queue();
	}

	@EventHandler(priority = -2)
	public void inform(@NotNull AutoDeleteFlagedEvent event) {
		event.getMessage().getAuthor().openPrivateChannel().flatMap(ch -> ch.sendMessageEmbeds(new EmbedBuilder()
				.setTitle("Nachricht gelöscht")
				.setColor(bot.getColor(event.getMessage().getGuild()))
				.setDescription("Deine Nachricht in " + event.getChannel().getAsMention() + " wurde gelöscht, da sie nicht den Filtern für den Kanal entspricht")
				.addField(
						"Folgende Nachrichten werden akzeptiert",
						bot.loadGuild(event.getMessage().getGuild()).getAutodelete(event.getChannel())
								.filter(f -> !f.isEmpty())
								.map(f -> "```" +
										f.stream()
												.map(AutoDeleteFlag::getName)
												.collect(Collectors.joining("\n")) +
										"```"
								)
								.orElse("*Keine*"),
						false
				)
				.build()
		)).queue();
	}

	@NotNull
	private String getThreadName(@NotNull Message message) {
		String content = message.getContentRaw()
				.replaceAll(URL_PATTERN.pattern(), "")
				.replaceAll("<a?:\\w+:(\\d+)>", "")
				.trim();

		content = replace(content, "<@(\\d+)>", g -> "@" + Optional.ofNullable(message.getGuild().getMemberById(g)).map(Member::getEffectiveName).orElse("Unbekannt"));
		content = replace(content, "<@&(\\d+)>", g -> "@" + Optional.ofNullable(bot.getJda().getRoleById(g)).map(Role::getName).orElse("Unbekannt"));
		content = replace(content, "<#(\\d+)>", g -> "#" + Optional.ofNullable(bot.getJda().getChannelById(Channel.class, g)).map(Channel::getName).orElse("Unbekannt"));

		if (content.isEmpty()) {
			if (!message.getEmbeds().isEmpty()) {
				String title = message.getEmbeds().get(0).getTitle();
				if (title != null) return title;
			}

			return "";
		}

		return content;
	}

	@NotNull
	private String replace(@NotNull String str, @NotNull String pattern, @NotNull Function<String, String> handler) {
		StringBuilder result = new StringBuilder();
		Matcher matcher = Pattern.compile(pattern).matcher(str);

		while (matcher.find()) {
			matcher.appendReplacement(result, handler.apply(matcher.group(1)));
		}

		matcher.appendTail(result);
		return result.toString();
	}
}
