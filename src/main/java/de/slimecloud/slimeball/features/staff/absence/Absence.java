package de.slimecloud.slimeball.features.staff.absence;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class Absence implements DataClass<Absence>, ListEntry {
	private final SlimeBot bot;

	@Column(key = true)
	private UserSnowflake teamMember;

	@Column()
	private final Guild guild;

	@Column
	private Instant time;

	public Absence(SlimeBot bot) {
		this(bot, null, null, null);
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

	public Runnable check() {
		List<Absence> absences = bot.getAbsences().expiredAbsence(Instant.now().atZone(Main.timezone).toInstant());
		if (absences.isEmpty()) return null;
		absences.forEach(absence -> {
			bot.loadGuild(absence.getGuild()).getAbsenceRole().ifPresent(role -> absence.getGuild().removeRoleFromMember(absence.getTeamMember(), role).queue());
			bot.getAbsences().remove(absence);
		});
		return null;
	}
}
