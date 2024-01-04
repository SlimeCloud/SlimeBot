package de.slimecloud.slimeball.features.reminder;

import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class Reminder implements DataClass<Reminder>, Comparable<Reminder> {
	private final SlimeBot bot;

	@Column(autoincrement = true, key = true)
	private final int id;

	@Column
	private final Guild guild;
	@Column
	private final UserSnowflake user;
	@Column
	@Nullable
	private final long roleId;

	@Column
	private final Instant time;

	@Column
	private final String message;

	public Reminder(@NotNull SlimeBot bot) {
		this(bot, 0, null, null, 0, null, null);
	}

	public void execute() {
		if(roleId == 0) {
			// Send Private Reminder
			EmbedBuilder embedBuilder = new EmbedBuilder()
					.setTitle("Reminder!")
					.setColor(bot.getColor(guild))
					.addField("Nachricht:", message, false)
					.setFooter("Reminder auf: " + guild.getName());

			bot.getJda().openPrivateChannelById(user.getIdLong())
					.flatMap(channel -> channel.sendMessageEmbeds(embedBuilder.build()))
					.queue();
		}else {
			// Send Role Reminder
			guild.retrieveMember(user).map(Member::getEffectiveName).queue(name -> {
				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setTitle("Reminder!")
						.setColor(bot.getColor(guild))
						.addField("Nachricht:", message, false)
						.setFooter("Reminder von: " + name);

				bot.loadGuild(guild.getIdLong()).getTeamChannel().ifPresent(channel -> {
					channel.sendMessage(bot.getJda().getRoleById(roleId).getAsMention()).setEmbeds(embedBuilder.build()).queue();
				});
			});
		}

		delete();
		bot.getRemindManager().scheduleNextReminder();
	}

	@NotNull
	@Override
	public Table<Reminder> getTable() {
		return bot.getReminder();
	}

	@Override
	public int compareTo(@NotNull Reminder o) {
		return this.getTime().compareTo(o.getTime());
	}
}