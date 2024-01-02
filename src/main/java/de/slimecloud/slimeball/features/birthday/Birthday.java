package de.slimecloud.slimeball.features.birthday;

import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Birthday implements DataClass<Birthday>, Comparable<Birthday> {
	private final SlimeBot bot;


	@Column(key = true)
	private final long guild;
	@Column(key = true)
	private final UserSnowflake user;

	@Column
	private final LocalDateTime date;

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
		if (date.isBefore(o.date)) return 1;
		if (date.isEqual(o.date)) return 0;
		return -1;
	}
}
