package de.slimecloud.slimeball.features.birthday;

import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public interface BirthdayTable extends Table<Birthday> {

	default void remove(@NotNull Member member) {
		remove(member.getGuild().getIdLong(), member);
	}

	default void remove(long guild, @NotNull UserSnowflake user) {
		get(guild, user).ifPresent(Birthday::delete);
	}

	@NotNull
	default Birthday set(@NotNull Member member, @NotNull Instant date) {
		return set(member.getGuild().getIdLong(), member, date);
	}

	@NotNull
	default Birthday set(long guild, @NotNull UserSnowflake user, @NotNull Instant date) {
		return new Birthday(getManager().getData("bot"), guild, user, date).update();
	}

	@NotNull
	default Optional<Birthday> get(@NotNull Member member) {
		return get(member.getGuild().getIdLong(), member);
	}

	@NotNull
	default Optional<Birthday> get(long guild, @NotNull UserSnowflake user) {
		return selectOne(Where.allOf(
				Where.equals("user", user.getIdLong()),
				Where.equals("guild", guild)
		));
	}

}
