package de.slimecloud.slimeball.features.alerts;

import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import de.slimecloud.slimeball.features.StoredId;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public interface IdMemory extends Table<StoredId> {
	default void rememberIds(@NotNull String type, @NotNull Collection<String> ids) {
		ids.forEach(id -> insert(new StoredId(type, id)));
	}

	default Set<String> getMemory(@NotNull String type) {
		return selectMany(Where.equals("type", type)).stream().map(StoredId::getId).collect(Collectors.toSet());
	}
}
