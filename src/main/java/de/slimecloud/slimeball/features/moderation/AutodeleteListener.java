package de.slimecloud.slimeball.features.moderation;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.JEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class AutodeleteListener extends ListenerAdapter {
	private final SlimeBot bot;

	public AutodeleteListener(@NotNull SlimeBot bot) {
		this.bot = bot;
		JEvent.getManager().registerListener(this);
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
				new ErrorHandler(System.out::println).ignore(ErrorResponse.UNKNOWN_MESSAGE)
		);
	}

	private void check(@NotNull Message message, @NotNull Channel channel, boolean thread) {
		if (!message.isFromGuild()) return;
		if (message.isWebhookMessage()) return;

		bot.loadGuild(message.getGuild()).getAutodelete(channel).ifPresent(filters -> {
			if (filters.stream().anyMatch(f -> f.getFilter().test(message))) return;
			new AutodeleteFlagedEvent(thread, channel, message).callEvent();
		});
	}

	@EventHandler(priority = -1)
	public void delete(@NotNull AutodeleteFlagedEvent event) {
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
	public void inform(@NotNull AutodeleteFlagedEvent event) {
		event.getMessage().getAuthor().openPrivateChannel().flatMap(ch -> ch.sendMessageEmbeds(new EmbedBuilder()
				.setTitle("Nachricht gelöscht")
				.setColor(bot.getColor(event.getMessage().getGuild()))
				.setDescription("Deine Nachricht in " + event.getChannel().getAsMention() + " wurde gelöscht, da sie nicht den Filtern für den Kanal entspricht")
				.addField(
						"Folgende Nachrichten werden akzeptiert",
						bot.loadGuild(event.getMessage().getGuild()).getAutodelete(event.getChannel())
								.map(f -> "```" +
										f.stream()
												.map(AutodleteFlag::getName)
												.collect(Collectors.joining("\n")) +
										"```"
								)
								.orElse("*Keine*"),
						false
				)
				.build()
		)).queue();
	}
}
