package de.slimecloud.slimeball.features.staff.absence;

import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AbsenceTable extends Table<Absence> {

	default Absence addAbsence(@NotNull Absence absence) {
		insert(absence);
		return absence;
	}

	default void remove(@NotNull Absence absence) {
		delete(absence);
	}

	default List<Absence> getExpiredAbsence(@NotNull Instant now) {
		return selectMany(Where.lowerOrEqual("time", now));
	}

	default @NotNull Optional<Absence> getByUser(@NotNull UserSnowflake user) {
		return selectOne(Where.equals("teammember", user));
	}
}
