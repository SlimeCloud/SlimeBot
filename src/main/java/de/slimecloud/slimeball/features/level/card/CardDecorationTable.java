package de.slimecloud.slimeball.features.level.card;

import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public interface CardDecorationTable extends Table<UserCardDecoration> {
	default Optional<UserCardDecoration> get(@NotNull Member member) {
		return selectOne(Where.allOf(
				Where.equals("guild", member.getGuild()),
				Where.equals("user", member.getUser())
		));
	}

	default Set<String> getDecorations(@NotNull Member member) {
		return get(member).map(UserCardDecoration::getDecorations).orElse(Collections.emptySet());
	}

	default void grantDecoration(@NotNull Member member, @NotNull String name) {
		UserCardDecoration decoration = get(member).orElseGet(() -> UserCardDecoration.empty(getManager().getData("bot"), member));

		decoration.getDecorations().add(name);

		insert(decoration);
	}

	default void revokeDecoration(@NotNull Member member, @NotNull String name) {
		UserCardDecoration decoration = get(member).orElseGet(() -> UserCardDecoration.empty(getManager().getData("bot"), member));

		decoration.getDecorations().remove(name);

		insert(decoration);
	}
}
