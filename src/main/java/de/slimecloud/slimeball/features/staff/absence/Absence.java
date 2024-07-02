package de.slimecloud.slimeball.features.staff.absence;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.exception.ConflictException;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class Absence implements DataClass<Absence>, ListEntry {
	private final SlimeBot bot;

	@Column(key = true)
	private UserSnowflake teamMember;

	@Column
	private Instant time;

	public Absence(@NotNull SlimeBot bot, @NotNull UserSnowflake teamMember) {
		this(bot, teamMember, LocalDateTime.now().atZone(Main.timezone).toInstant());
	}

	@NotNull
	@Override
	public Table<Absence> getTable() {
		return bot.getAbsences();
	}

	@NotNull
	@Override
	public String build(int index, @NotNull ListContext<? extends ListEntry> context) {
		return "";
	}
}
