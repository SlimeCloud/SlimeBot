package de.slimecloud.slimeball.features.birthday;

import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
public class Birthday implements DataClass<Birthday>, ListEntry, Comparable<Birthday> {
	private final SlimeBot bot;


	@Column(key = true)
	private final long guild;
	@Column(key = true)
	private final UserSnowflake user;

	@Column
	private final Instant instant;

	public Birthday(SlimeBot bot) {
		this(bot, 0, null, null);
	}

	@NotNull
	@Override
	public Table<Birthday> getTable() {
		return bot.getBirthdayTable();
	}

	@NotNull
	@Override
	public Birthday update() {
		return (Birthday) DataClass.super.update();
	}

	@NotNull
	@Override
	public Birthday delete() {
		return (Birthday) DataClass.super.delete();
	}

	@Override
	public int compareTo(@NotNull Birthday o) {
		return getNextBirthday().compareTo(o.getNextBirthday());
	}

	@NotNull
	@Override
	public String build(int index, @NotNull ListContext<? extends ListEntry> context) {
		return String.format("%s %s", TimeFormat.RELATIVE.format(getNextBirthday()), user.getAsMention());
	}

	public ZonedDateTime getNextBirthday() {
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime zdt = instant.atZone(zone);
		ZonedDateTime now = ZonedDateTime.now();
		zdt = LocalDateTime.of(now.getYear(), zdt.getMonth(), zdt.getDayOfMonth(), 0, 0).atZone(zone);

		boolean passed = now.isAfter(zdt);
		int nextBdYear = now.getYear() + (passed ? 1 : 0);
		LocalDateTime ldt = LocalDateTime.of(nextBdYear, zdt.getMonth(), zdt.getDayOfMonth(), 0, 0);
		return ldt.atZone(zone);
	}

}
