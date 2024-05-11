package de.slimecloud.slimeball.features.poll;

import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public interface PollTable extends Table<Poll> {
	@NotNull
	default Poll createPoll(long message, int max, boolean names, @NotNull String[] choices) {
		return insert(new Poll(
				getManager().getData("bot"),
				message,
				max,
				names,
				Arrays.stream(choices).collect(Collectors.toMap(c -> c, x -> new ArrayList<>(), (x, y) -> y, LinkedHashMap::new))
		));
	}

	default void delete(long message) {
		delete(Where.equals("id", message));
	}

	@NotNull
	default Optional<Poll> getPoll(long message) {
		return selectOne(Where.equals("id", message));
	}
}
