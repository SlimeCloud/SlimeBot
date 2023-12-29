package de.slimecloud.slimeball.main.extensions;

import de.mineking.javautils.database.DatabaseManager;
import de.mineking.javautils.database.TypeMapper;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jdbi.v3.core.argument.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserSnowflakeTypeMapper implements TypeMapper<Long, UserSnowflake> {
	@Override
	public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field) {
		return UserSnowflake.class.isAssignableFrom(type);
	}

	@NotNull
	@Override
	public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field) {
		return "bigint";
	}

	@NotNull
	@Override
	public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable UserSnowflake value) {
		return (pos, stmt, ctx) -> stmt.setLong(pos, value == null ? null : value.getIdLong());
	}

	@NotNull
	@Override
	public String string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable UserSnowflake value) {
		return value == null ? "null" : value.getId();
	}

	@Nullable
	@Override
	public Long extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
		return set.getLong(name);
	}

	@Nullable
	@Override
	public UserSnowflake parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable Long value) {
		return value == null ? null : UserSnowflake.fromId(value);
	}
}
