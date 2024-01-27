package de.slimecloud.slimeball.features.reminder;

import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.main.SlimeEmoji;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public class Reminder implements DataClass<Reminder>, Comparable<Reminder>, Runnable, ListEntry {
	private final SlimeBot bot;

	@Column(autoincrement = true, key = true)
	private final int id;

	@Column
	private final Guild guild;
	@Column
	private final UserSnowflake user;

	@Column
	@Nullable
	private final Role role;

	@Column
	private final Instant time;
	@Column
	private final Instant timeSet;

	@Column
	private final String message;

	public Reminder(@NotNull SlimeBot bot) {
		this(bot, 0, null, null, null, null, null, null);
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

	@Override
	public void run() {
		if (role == null) {
			// Send Private Reminder
			EmbedBuilder embedBuilder = new EmbedBuilder()
					.setAuthor(guild.getName(), null, guild.getIconUrl())
					.setTitle(SlimeEmoji.EXCLAMATION.toString(guild) + " Reminder!")
					.setColor(bot.getColor(guild))
					.setDescription(message + " \n \n" + "(Reminder von " + TimeFormat.RELATIVE.format(timeSet) + ")");

			bot.getJda().openPrivateChannelById(user.getIdLong())
					.flatMap(channel -> channel.sendMessageEmbeds(embedBuilder.build()))
					.queue();
		} else {
			// Send Role Reminder
			guild.retrieveMember(user).queue(member -> {
				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
						.setTitle(SlimeEmoji.EXCLAMATION.toString(guild) + " Reminder!")
						.setColor(bot.getColor(guild))
						.setDescription(message + " \n \n" + "(Reminder von " + TimeFormat.RELATIVE.format(timeSet) + ")");

				bot.loadGuild(guild.getIdLong()).getTeamChannel().ifPresent(channel -> {
					channel.sendMessage(role.getAsMention()).setEmbeds(embedBuilder.build()).queue();
				});
			});
		}

		delete();
		bot.getRemindManager().scheduleNextReminder();
	}

	public Optional<ScheduledFuture<?>> schedule() {
		long delay = time.toEpochMilli() - System.currentTimeMillis();
		if (delay <= 0) {
			run();
			return Optional.empty();
		}
		return Optional.of(bot.getExecutor().schedule(this, delay / 1000, TimeUnit.SECONDS));
	}

	@NotNull
	@Override
	public String build(int index, @NotNull ListContext<? extends ListEntry> context) {
		return (index + 1) + ". " + TimeFormat.RELATIVE.format(time) + ": " + message;
	}
}