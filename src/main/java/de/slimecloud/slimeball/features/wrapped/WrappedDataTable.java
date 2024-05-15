package de.slimecloud.slimeball.features.wrapped;

import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WrappedDataTable extends Table<WrappedData> {
	@NotNull
	default WrappedData getData(@NotNull Guild guild, @NotNull UserSnowflake user) {
		return selectOne(Where.allOf(
				Where.equals("guild", guild),
				Where.equals("user", user)
		)).orElseGet(() -> WrappedData.empty(getManager().getData("bot"), guild, user));
	}

	@NotNull
	default WrappedData getData(@NotNull Member member) {
		return getData(member.getGuild(), member);
	}

	@NotNull
	default List<WrappedData> getAll(@NotNull Guild guild) {
		return selectMany(Where.equals("guild", guild));
	}
}
