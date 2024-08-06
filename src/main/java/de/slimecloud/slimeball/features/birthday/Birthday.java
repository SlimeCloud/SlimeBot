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

import java.sql.Date;
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
	private final Date date;

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

	@SuppressWarnings("deprecation")
	public boolean isBirthday() {
		ZonedDateTime now = ZonedDateTime.now(Main.timezone);
		Date today = new Date((now.toEpochSecond() + now.getOffset().getTotalSeconds()) * 1000);

		Date current = (Date) date.clone();
		current.setYear(today.getYear());

		return current.equals(today);
	}

	@NotNull
	@SuppressWarnings("deprecation")
	public Date getNextBirthday() {
		ZonedDateTime now = ZonedDateTime.now(Main.timezone);
		Date today = new Date((now.toEpochSecond() + now.getOffset().getTotalSeconds()) * 1000);

		Date current = (Date) date.clone();
		current.setYear(today.getYear());

		if (today.after(current)) current.setYear(current.getYear() + 1);

		return current;
	}

	@NotNull
	public String formatNext() {
		ZonedDateTime now = ZonedDateTime.now(Main.timezone);
		ZonedDateTime bd = getNextBirthday().toInstant().atZone(Main.timezone);

		if (bd.getYear() == now.getYear() && bd.getDayOfYear() == now.getDayOfYear()) return "`Heute`";
		return TimeFormat.RELATIVE.format(bd);
	}
}
