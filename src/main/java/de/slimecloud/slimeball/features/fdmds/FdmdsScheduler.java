package de.slimecloud.slimeball.features.fdmds;

import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.main.SlimeEmoji;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FdmdsScheduler {
	private final SlimeBot bot;

	public FdmdsScheduler(@NotNull SlimeBot bot) {
		this.bot = bot;
		bot.getScheduler().scheduleDaily(9, () -> bot.getJda().getGuilds().forEach(this::send));
	}

	public void send(@NotNull Guild guild) {
		bot.loadGuild(guild).getFdmds().ifPresent(config -> {
			Queue<FdmdsQueueItem> items = new LinkedList<>(bot.getFdmdsQueue().getNextItems(guild, 3));
			if (items.isEmpty()) config.getLogChannel().sendMessage(":warning: Keine Umfragen zum senden").queue();
			else nextItem(config, items);
		});
	}

	public void nextItem(FdmdsConfig config, @NotNull Queue<FdmdsQueueItem> items) {
		FdmdsQueueItem item = items.poll();
		if (item == null) return;

		config.getLogChannel().retrieveMessageById(item.getMessage()).queue(message -> {
			//Retry with next entry on failure
			if (!sendFdmds(bot, config, message, true)) nextItem(config, items);
		});
	}

	public static boolean sendFdmds(@NotNull SlimeBot bot, @NotNull FdmdsConfig config, @NotNull Message message, boolean automatic) {
		try {
			//Load information from embed
			MessageEmbed embed = message.getEmbeds().get(0);

			String question = embed.getDescription();
			String title = embed.getTitle();
			String[] choices = embed.getFields().get(0).getValue().split("\n");

			UserSnowflake user = UserSnowflake.fromId(embed.getFooter().getText().substring("Nutzer ID: ".length()));

			//Call event
			new FdmdsCreateEvent(user, message.getGuild(), question).callEvent();

			MessagePollBuilder builder = MessagePollData.builder(question)
					.setMultiAnswer(true)
					.setDuration(Duration.ofDays(7));

			for (int i = 0; i < choices.length; i++) builder.addAnswer(choices[i].split(" -> ", 2)[1], SlimeEmoji.number(i).getEmoji(message.getGuild()));

			config.getChannel().sendMessagePoll(builder.build())
					.setContent(config.getRole().map(Role::getAsMention).orElse(null))
					.addContent("\n# " + title)
					.addContent("\n" + user.getAsMention() + " fragt")
					.addActionRow(Button.secondary("fdmds:create", "Selbst eine Frage einreichen"))
					.queue(m -> {
						m.createThreadChannel(title).queue();
						bot.getFdmdsQueue().removeItemFromQueue(message.getIdLong());

						message.editMessage("Gesendet - " + m.getJumpUrl()).setComponents().queue();

						if (automatic) {
							List<FdmdsQueueItem> queue = bot.getFdmdsQueue().getNextItems(message.getGuild(), 5);
							AtomicInteger i = new AtomicInteger(1);
							message.reply("### Umfrage gesendet, " + (queue.size() <= 1 ? ":warning: " : "") + queue.size() + " weitere Umfragen in der Queue\n" + queue.stream()
									.map(element -> i.getAndIncrement() + ". [" + element.getTitle() + "](" + Message.JUMP_URL.formatted(message.getGuild().getIdLong(), message.getChannel().getIdLong(), element.getMessage()) + ")")
									.collect(Collectors.joining("\n"))
							).setSuppressedNotifications(true).queue();
						}
					});

			return true;
		} catch (Exception e) {
			message.reply(":x: Fehler beim senden dieser Umfrage: " + e.getMessage() + "\n-# Einreichung wurde aus Queue entfernt").queue();

			bot.getFdmdsQueue().removeItemFromQueue(message.getIdLong());
			message.editMessageComponents(FdmdsCommand.getComponents(true)).queue();

			return false;
		}
	}
}
