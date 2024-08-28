package de.slimecloud.slimeball.features.level.card;

import de.mineking.databaseutils.Where;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum Filter {
	ALL(x -> Where.empty(), "Alle"),
	OWN(user -> Where.equals("owner", user), "Eigene");

	private final Function<UserSnowflake, Where> filter;
	private final String name;
}
