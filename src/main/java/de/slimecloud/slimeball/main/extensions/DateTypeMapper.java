package de.slimecloud.slimeball.main.extensions;

import de.mineking.databaseutils.DatabaseManager;
import de.mineking.databaseutils.TypeMapper;
import de.mineking.databaseutils.type.DataType;
import de.mineking.databaseutils.type.PostgresType;
import de.mineking.javautils.reflection.ReflectionUtils;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class DateTypeMapper implements TypeMapper<Date, Date> {
	@Override
	public boolean accepts(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field f) {
		return ReflectionUtils.getClass(type).isAssignableFrom(Date.class);
	}

	@NotNull
	@Override
	public DataType getType(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field f) {
		return PostgresType.DATE;
	}

	@NotNull
	@Override
	public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field f, @Nullable Date value) {
		return new Argument() {
			@Override
			public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
				statement.setDate(position, value);
			}

			@Override
			public String toString() {
				return Objects.toString(value);
			}
		};
	}

	@Nullable
	@Override
	public Date extract(@NotNull ResultSet set, @NotNull String name, @NotNull Type target) throws SQLException {
		return set.getDate(name);
	}
}
