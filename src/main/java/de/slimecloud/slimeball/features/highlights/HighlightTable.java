package de.slimecloud.slimeball.features.highlights;

import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import de.slimecloud.slimeball.features.birthday.event.BirthdayRemoveEvent;
import de.slimecloud.slimeball.features.highlights.event.HighlightRemoveEvent;
import de.slimecloud.slimeball.features.highlights.event.HighlightSetEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface HighlightTable extends Table<Highlight> {

	@NotNull
	default Highlight build(@NotNull Guild guild, @NotNull String phrase, @NotNull Set<UserSnowflake> users) {
		return new Highlight(getManager().getData("bot"), guild, phrase, users);
	}

	@NotNull
	default Highlight save(@NotNull Guild guild, @NotNull String phrase, @NotNull Set<UserSnowflake> users) {
		return upsert(build(guild, phrase, users));
	}

	@NotNull
	default Highlight set(@NotNull Member member, @NotNull String phrase) {
		Set<UserSnowflake> users = getUsers(member.getGuild(), phrase);
		Highlight highlight = build(member.getGuild(), phrase, users);
		if (!users.contains(member) && !new HighlightSetEvent(highlight, member).callEvent()) {
			users.add(member);
			return save(member.getGuild(), phrase, users);
		}
		return highlight;
	}

	/**
	 * @return null if the member has no highlight with this phrase
	 */
	@Nullable
	default Highlight remove(@NotNull Member member, @NotNull String phrase) {
		Set<UserSnowflake> users = getUsers(member.getGuild(), phrase);
		if (!users.contains(member.getUser())) return null;
		Highlight highlight = build(member.getGuild(), phrase, users);
		if (users.contains(member.getUser()) && !new HighlightRemoveEvent(highlight, member).callEvent()) {
			users.remove(member.getUser());
			return save(member.getGuild(), phrase, users);
		}
		return highlight;
	}

	@NotNull
	default Set<UserSnowflake> getUsers(@NotNull Guild guild, @NotNull String phrase) {
		return get(guild, phrase)
				.map(Highlight::getUsers)
				.orElseGet(HashSet::new);
	}

	@NotNull
	default Optional<Highlight> get(@NotNull Guild guild, @NotNull String phrase) {
		return selectOne(Where.allOf(
				Where.equals("guild", guild),
				Where.equals("phrase", phrase)
		));
	}

	@NotNull
	default List<Highlight> get(@NotNull Guild guild) {
		return selectMany(Where.equals("guild", guild));
	}

	@NotNull
	default List<Highlight> getHighlights(@NotNull UserSnowflake user) {
		return selectMany(Where.fieldContainsValue("users", user));
	}
}
