package de.slimecloud.slimeball.features.highlights;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.Listener;
import de.slimecloud.slimeball.features.highlights.event.HighlightTriggeredEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Listener
@RequiredArgsConstructor
public class HighlightListener extends ListenerAdapter {

	private final SlimeBot bot;

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (!event.isFromGuild() || event.getAuthor().isBot()) return;

		Set<Highlight> highlights = checkHighlight(event.getGuild(), event.getMessage().getContentDisplay().toLowerCase());
		highlights.forEach(h -> new HighlightTriggeredEvent(h, event.getMessage()).callEvent());
	}

	private Set<Highlight> checkHighlight(@NotNull Guild guild, @NotNull String msg) {
		List<Highlight> highlights = bot.getHighlights().get(guild);
		Set<Highlight> result = new HashSet<>();

		for (Highlight highlight : highlights) {
			if (msg.contains(highlight.getPhrase())) result.add(highlight);
		}

		return result;
	}


	@EventHandler
	public void onHighlight(@NotNull HighlightTriggeredEvent event) {
		Highlight highlight = event.getHighlight();
		Message msg = event.getMessage();
		User author = msg.getAuthor();

		for (UserSnowflake snowflake : highlight.getUsers()) {
			User user = bot.getJda().getUserById(snowflake.getIdLong());

			if (user != null && !author.equals(user)) {
				user.openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(new EmbedBuilder()
						.setTitle("Highlight - " + highlight.getPhrase())
						.setAuthor(author.getName(), null, author.getEffectiveAvatarUrl())
						.setDescription(String.format("Dein Highlight `%s` wurde von %s in %s in einer **[Nachricht](%s)** erwähnt", highlight.getPhrase(), author.getAsMention(), msg.getChannel().getAsMention(), msg.getJumpUrl()))
						.setColor(bot.getColor(event.getGuild()))
						.build()
				)).queue();
			}
		}
	}
}
