package de.slimecloud.slimeball.main.extensions;

import de.mineking.javautils.database.DatabaseManager;
import de.mineking.javautils.database.TypeMapper;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;

public class LocalDateTimeTypeMapper implements TypeMapper<Long, LocalDateTime> {
	@Override
	public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
		return LocalDateTime.class.equals(type);
	}

	@NotNull
	@Override
	public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
		return "bigint";
	}

	@NotNull
	@Override
	public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable LocalDateTime value) {
		return new Argument() {
			@Override
			public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
				if (value == null) statement.setObject(position, null);
				else {
					ZonedDateTime zdt = ZonedDateTime.of(value, ZoneId.systemDefault());
					statement.setLong(position, zdt.toInstant().toEpochMilli());
				}
			}

			@Override
			public String toString() {
				return String.valueOf(value);
			}
		};
	}

	@NotNull
	@Override
	public String string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable LocalDateTime value) {
		return String.valueOf(value);
	}

	@Nullable
	@Override
	public Long extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
		return set.getLong(name);
	}

	@Nullable
	@Override
	@Contract("_, _, _, null -> null")
	public LocalDateTime parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable Long value) {
		if (value==null) return null;

		Instant instant = Instant.ofEpochMilli(value);
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}
}
