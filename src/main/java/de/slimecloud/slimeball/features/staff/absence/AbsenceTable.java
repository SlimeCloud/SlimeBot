package de.slimecloud.slimeball.features.staff.absence;

import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AbsenceTable extends Table<Absence> {

	@NotNull
	default Absence addMember(@NotNull SlimeBot bot, @NotNull Member member, @NotNull Instant time) {
		return insert(new Absence(bot, member.getUser(), member.getGuild(), time));
	}

	default void remove(@NotNull Member member) {
		delete(Where.allOf(
				Where.equals("teamMember", member),
				Where.equals("guild", member.getGuild())
				));
	}

	@NotNull
	default List<Absence> getExpiredAbsence(@NotNull Instant now) {
		return selectMany(Where.lowerOrEqual("time", now));
	}

	@NotNull
	default Optional<Absence> getByUser(@NotNull Member member) {
		return selectOne(Where.equals("teammember", member.getUser()));
	}
}
