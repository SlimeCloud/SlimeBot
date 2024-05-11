package de.slimecloud.slimeball.features.birthday;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZonedDateTime;

@Getter
@ToString
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
		return String.format("%s %s", formatNext(), user.getAsMention());
	}


	@NotNull
	public ZonedDateTime getStart() {
		return time.atZone(Main.timezone).withYear(ZonedDateTime.now(Main.timezone).getYear())
				.withHour(0)
				.withMinute(0)
				.withSecond(0);
	}

	@NotNull
	public ZonedDateTime getEnd() {
		return time.atZone(Main.timezone).withYear(ZonedDateTime.now(Main.timezone).getYear())
				.withHour(23)
				.withMinute(59)
				.withSecond(59);
	}

	public boolean isBirthday(@NotNull ZonedDateTime time) {
		return time.isAfter(getStart()) && time.isBefore(getEnd());
	}

	@NotNull
	public ZonedDateTime getNextBirthday() {
		ZonedDateTime now = ZonedDateTime.now(Main.timezone);
		return now.isAfter(getEnd()) ? getStart().withYear(now.getYear() + 1) : getStart();
	}

	@NotNull
	public String formatNext() {
		ZonedDateTime now = ZonedDateTime.now(Main.timezone);
		ZonedDateTime bd = getNextBirthday();

		if (bd.getYear() == now.getYear() && bd.getDayOfYear() == now.getDayOfYear()) return "`Heute`";
		return TimeFormat.RELATIVE.format(bd);
	}
}
