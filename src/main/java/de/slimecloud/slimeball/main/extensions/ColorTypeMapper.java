package de.slimecloud.slimeball.main.extensions;

import de.mineking.javautils.database.DatabaseManager;
import de.mineking.javautils.database.TypeMapper;
import de.mineking.javautils.database.type.DataType;
import de.mineking.javautils.database.type.PostgresType;
import de.slimecloud.slimeball.util.ColorUtil;
import org.jdbi.v3.core.argument.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColorTypeMapper implements TypeMapper<Integer, Color> {
	@Override
	public boolean accepts(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field f) {
		return type.equals(Color.class);
	}

	@NotNull
	@Override
	public DataType getType(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field f) {
		return PostgresType.INTEGER;
	}

	@NotNull
	@Override
	public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field f, @Nullable Integer value) {
		return (pos, stmt, ctx) -> stmt.setObject(pos, value);
	}

	@Nullable
	@Override
	public Integer format(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field f, @Nullable Color value) {
		return value == null ? null : value.getRGB();
	}

	@Nullable
	@Override
	public Integer extract(@NotNull ResultSet set, @NotNull String name, @NotNull Type target) throws SQLException {
		return set.getInt(name);
	}

	@Nullable
	@Override
	public Color parse(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field field, @Nullable Integer value) {
		return value == null ? null : ColorUtil.ofCode(value);
	}
}
