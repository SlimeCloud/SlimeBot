package de.slimecloud.slimeball.features.fdmds;

import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.main.SlimeEmoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class FdmdsScheduler {
	private final SlimeBot bot;

	public FdmdsScheduler(@NotNull SlimeBot bot) {
		this.bot = bot;
		bot.scheduleDaily(9, () -> bot.getJda().getGuilds().forEach(this::send));
	}

	public void send(@NotNull Guild guild) {
		bot.loadGuild(guild).getFdmds().ifPresent(config -> bot.getFdmdsQueue().getNextItem(guild).ifPresentOrElse(
				item -> config.getLogChannel().retrieveMessageById(item.getMessage()).queue(message -> {
					//Load information from embed
					MessageEmbed embed = message.getEmbeds().get(0);

					String question = embed.getDescription();
					String title = embed.getTitle();
					String[] choices = embed.getFields().get(0).getValue().split("\n");

					UserSnowflake user = UserSnowflake.fromId(embed.getFooter().getText().substring("Nutzer ID: ".length()));

					//Call event
					new FdmdsCreateEvent(user, guild, question).callEvent();

					MessagePollBuilder builder = MessagePollData.builder(question)
							.setMultiAnswer(true)
							.setDuration(Duration.ofDays(7));

					for (int i = 0; i < choices.length; i++) builder.addAnswer(choices[i].split(" -> ", 2)[1], SlimeEmoji.number(i).getEmoji(item.getGuild()));

					config.getChannel().sendMessagePoll(builder.build())
							.setContent(config.getRole().map(Role::getAsMention).orElse(null))
							.addContent("\n# " + title)
							.addContent("\n" + user.getAsMention() + " fragt")
							.addActionRow(Button.secondary("fdmds:create", "Selbst eine Frage einreichen"))
							.queue(m -> m.createThreadChannel(title).queue());

					message.delete().queue();
				}),
				() -> config.getLogChannel().sendMessage("Keine Umfragen zum senden").queue()
		));
	}
}
