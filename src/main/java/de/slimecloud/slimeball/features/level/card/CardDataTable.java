package de.slimecloud.slimeball.features.level.card;

import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface CardDataTable extends Table<CardProfileData> {
	@NotNull
	default Optional<CardProfileData> getData(int id, @NotNull UserSnowflake owner) {
		if(id <= 0) return Optional.of(new CardProfileData(getManager().getData("bot"), owner));
		return selectOne(Where.equals("id", id));
	}

	@NotNull
	default List<CardProfileData> getAll(@NotNull UserSnowflake owner) {
		return selectMany(Where.equals("owner", owner.getIdLong()));
	}
}
