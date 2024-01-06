package de.slimecloud.slimeball.features.birthday;

import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
public class Birthday implements DataClass<Birthday>, ListEntry, Comparable<Birthday> {
	private final SlimeBot bot;

	@Column(key = true)
	private final Guild guild;
	@Column(key = true)
	private final UserSnowflake user;

	@Column
	private final Instant time;

	public Birthday(@NotNull SlimeBot bot) {
		this(bot, null, null, null);
	}

	@NotNull
	@Override
	public Table<Birthday> getTable() {
		return bot.getBirthdays();
	}

	@Override
	public int compareTo(@NotNull Birthday o) {
		return getNextBirthday().compareTo(o.getNextBirthday());
	}

	@NotNull
	@Override
	public String build(int index, @NotNull ListContext<? extends ListEntry> context) {
		int age = getAge();
		return String.format("%s %s%s", getFormat(), user.getAsMention(), age==-1 ? "" : String.format(" wird %s Jahre alt!", ++age));
	}

	public int getAge() {
		ZonedDateTime date = time.atZone(SlimeBot.timezone);
		ZonedDateTime now = ZonedDateTime.now(SlimeBot.timezone);
		if (date.getYear()==0) return -1;
		int age = now.getYear()-date.getYear();
		if (!now.isAfter(date.withYear(now.getYear()))) age--;
		return age;
	}


	@NotNull
	public ZonedDateTime getNextBirthday() {
		ZonedDateTime now = ZonedDateTime.now(SlimeBot.timezone);
		ZonedDateTime date = time.atZone(SlimeBot.timezone).withYear(now.getYear());
		ZonedDateTime end = date.withHour(23).withMinute(59).withSecond(59);

		return date.withYear(now.isAfter(end) ? now.getYear() + 1 : now.getYear());
	}

	@NotNull
	public String getFormat() {
		ZonedDateTime zdt = getNextBirthday();
		ZonedDateTime now = ZonedDateTime.now();
		boolean today = zdt.getYear()==now.getYear() && zdt.getMonth()==now.getMonth() && zdt.getDayOfMonth()==now.getDayOfMonth();
		return today ? "`Heute`" : TimeFormat.RELATIVE.format(zdt);
	}

	@Override
	public String toString() {
		ZonedDateTime date = time.atZone(SlimeBot.timezone);
		return TimeFormat.DATE_TIME_SHORT.format(date);
	}
}
