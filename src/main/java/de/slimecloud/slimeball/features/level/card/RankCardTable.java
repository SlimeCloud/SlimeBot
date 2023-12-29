package de.slimecloud.slimeball.features.level.card;

import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

public interface RankCardTable extends Table<CardProfile> {
	@NotNull
	default CardProfile getProfile(@NotNull Member member) {
		return selectOne(Where.allOf(
				Where.equals("guild", member.getGuild().getIdLong()),
				Where.equals("user", member.getIdLong())
		)).orElseGet(() -> new CardProfile(getManager().getData("bot"), member));
	}

	default void reset(@NotNull Member member) {
		delete(Where.allOf(
				Where.equals("guild", member.getGuild().getIdLong()),
				Where.equals("user", member.getIdLong())
		));
	}
}
