package de.slimecloud.slimeball.features.staff.absence;

import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import de.mineking.discordutils.list.Listable;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AbsenceTable extends Table<Absence>, Listable<Absence> {

	default Absence addAbsence(Absence absence) {
		insert(absence);
		return absence;
	}

	default void remove(Absence absence) {
		delete(absence);
	}

	default List<Absence> expiredAbsence(Instant now) {
		return selectMany(Where.lowerOrEqual("time", now));
	}

	default Optional<Absence> getByUser(UserSnowflake user) {
		return selectOne(Where.equals("teammember", user));
	}
}
