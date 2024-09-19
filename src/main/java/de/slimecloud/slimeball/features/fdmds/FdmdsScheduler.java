package de.slimecloud.slimeball.features.fdmds;

import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.main.SlimeEmoji;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

public class FdmdsScheduler {
	private final SlimeBot bot;

	public FdmdsScheduler(@NotNull SlimeBot bot) {
		this.bot = bot;
		bot.scheduleDaily(9, () -> bot.getJda().getGuilds().forEach(this::send));
	}

	public void send(@NotNull Guild guild) {
		bot.loadGuild(guild).getFdmds().ifPresent(config -> {
			Queue<FdmdsQueueItem> items = new LinkedList<>(bot.getFdmdsQueue().getNextItems(guild));
			if (items.isEmpty()) config.getLogChannel().sendMessage("Keine Umfragen zum senden").setSuppressedNotifications(true).queue();
			else nextItem(config, items);
		});
	}

	public void nextItem(FdmdsConfig config, @NotNull Queue<FdmdsQueueItem> items) {
		FdmdsQueueItem item = items.poll();
		if (item == null) return;

		config.getLogChannel().retrieveMessageById(item.getMessage()).queue(message -> {
			//Retry with next entry on failure
			if (!sendFdmds(bot, config, message)) nextItem(config, items);
		});
	}

	public static boolean sendFdmds(@NotNull SlimeBot bot, @NotNull FdmdsConfig config, @NotNull Message message) {
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
					.queue(m -> m.createThreadChannel(title).queue());

			message.delete().queue(x -> bot.getFdmdsQueue().removeItemFromQueue(message.getIdLong()));

			return true;
		} catch (Exception e) {
			message.reply("Fehler beim senden dieser Umfrage: " + e.getMessage() + "\n-# Einreichung wurde aus Queue entfernt").setSuppressedNotifications(true).queue();

			bot.getFdmdsQueue().removeItemFromQueue(message.getIdLong());
			message.editMessageComponents(ActionRow.of(
					Button.secondary("fdmds:edit", "Bearbeiten"),
					Button.primary("fdmds:add", "Hinzufügen")
			)).queue();

			return false;
		}
	}
}
