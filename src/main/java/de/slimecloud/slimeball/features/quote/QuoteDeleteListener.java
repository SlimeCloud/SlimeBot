package de.slimecloud.slimeball.features.quote;

import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class QuoteDeleteListener extends ListenerAdapter {
	private final SlimeBot bot;

	@Override
	public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
		bot.loadGuild(event.getGuild()).getQuoteChannel().ifPresent(channel -> {
			if (event.getChannel().getIdLong() != channel.getIdLong()) return;
			if (!event.getEmoji().getAsReactionCode().equals("❌")) return;

			event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> {
				if (event.getUserIdLong() != message.getMentions().getUsers().get(0).getIdLong()) return;
				if (!new QuoteDeleteEvent(event.getMember()).callEvent()) message.delete().queue();
			});

			event.getReaction().removeReaction(event.getUser()).queue();
		});
	}
}
