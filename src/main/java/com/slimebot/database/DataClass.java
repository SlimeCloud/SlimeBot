package com.slimebot.database;

import com.google.gson.Gson;
import com.slimebot.main.Main;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@EqualsAndHashCode
public abstract class DataClass {

	public static final Gson gson = new Gson();

	private transient String cache;

	protected DataClass() {
		updateCache();
		createTable();
	}

	public static boolean isValid(Field field) {
		int mods = field.getModifiers();
		return !(Modifier.isTransient(mods) || Modifier.isStatic(mods));
	}

	private void createTable() {
		List<String> keyTypes = new LinkedList<>();
		List<String> primaryKeys = new LinkedList<>();

		for (Field field : getClass().getDeclaredFields()) {
			if (!isValid(field)) continue;
			field.setAccessible(true);

			String name = field.getName().toLowerCase();
			if (field.isAnnotationPresent(Key.class)) primaryKeys.add('"' + name + '"');

			keyTypes.add('"' + name + "\" " + getDataType(field.getType()));
		}

		String sql = "create table if not exists %s(%s, primary key(%s))"
				.formatted(
						getTableName(),
						String.join(", ", keyTypes),
						String.join(", ", primaryKeys)
				);
		Main.database.run(handle -> handle.createUpdate(sql).execute());
	}

	private @Nullable String getDataType(@NotNull Class<?> clazz) {
		if (clazz.isEnum() || clazz.isAssignableFrom(EnumSet.class)) return "int";
		if (clazz.equals(byte.class) || clazz.equals(Byte.class) || clazz.equals(short.class) || clazz.equals(Short.class)) return "smallint";
		if (clazz.isAssignableFrom(int.class) || clazz.equals(Integer.class)) return "int";
		if (clazz.isAssignableFrom(long.class) || clazz.equals(Long.class)) return "bigint";
		if (clazz.isAssignableFrom(float.class) || clazz.equals(Float.class)) return "real";
		if (clazz.isAssignableFrom(double.class) || clazz.equals(Double.class)) return "float8";
		if (clazz.isAssignableFrom(char.class) || clazz.equals(Character.class)) return "char";
		if (clazz.isAssignableFrom(boolean.class) || clazz.equals(Boolean.class)) return "bool";
		if (clazz.isAssignableFrom(String.class)) return "text";
		return null;
	}

	protected void updateCache() {
		this.cache = gson.toJson(this);
	}

	public synchronized DataClass save() {
		Object cacheObj = gson.fromJson(cache, getClass());
		Map<String, Object> updatedValues = new HashMap<>();
		Set<String> keys = new HashSet<>();

		for (Field field : getClass().getDeclaredFields()) {
			if (!isValid(field)) continue;
			field.setAccessible(true);

			String name = field.getName().toLowerCase();
			if (field.isAnnotationPresent(Key.class)) keys.add(name);

			try {
				Object newVal = field.get(this);
				if (newVal.equals(field.get(cacheObj)) && !field.isAnnotationPresent(Key.class)) continue;
				updatedValues.put(name, field.getType().isEnum() ? ((Enum<?>) newVal).ordinal() : newVal);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

		}

		if (keys.containsAll(updatedValues.keySet())) return this;

		String sql = "insert into %s(%s) values(%s) on conflict(%s) do update set %s"
				.formatted(
						getTableName(),
						updatedValues.keySet().stream()
								.map(n -> '"' + n + '"')
								.collect(Collectors.joining(", ")),

						updatedValues.keySet().stream()
								.map(n -> ":" + n)
								.collect(Collectors.joining(", ")),

						keys.stream()
								.map(n -> '"' + n + '"')
								.collect(Collectors.joining(", ")),

						updatedValues.keySet().stream()
								.filter(n -> !keys.contains(n))
								.map(n -> '"' + n + "\" = :" + n)
								.collect(Collectors.joining(", "))
				);
		Main.database.run(handle -> handle.createUpdate(sql).bindMap(updatedValues).execute());

		updateCache();
		return this;
	}

	private static String buildSQL(String tableName, Map<String, Object> keys) {
		return "select * from %s where %s"
				.formatted(
						tableName,
						keys.keySet().stream()
								.map(n -> '"' + n.toLowerCase() + "\" = :" + n)
								.collect(Collectors.joining(" and "))
				);
	}

	private static <T extends DataClass> T setFields(T instance, ResultSet rs) throws SQLException {
		for (Field field : instance.getClass().getDeclaredFields()) {
			if (!isValid(field)) continue;
			field.setAccessible(true);
			try {
				field.set(instance, field.getType().isEnum() ? field.getType().getEnumConstants()[rs.getInt(field.getName().toLowerCase())] : get(field.getType(), rs, field.getName().toLowerCase()));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		instance.updateCache();
		return instance;
	}

	public static <T extends DataClass> List<T> loadAll(@NotNull Supplier<T> creator, @NotNull Map<String, Object> keys) {
		String sql = buildSQL(creator.get().getTableName(), keys);
		return Main.database.handle(handle -> handle.createQuery(sql).bindMap(keys).map((rs, ctx) -> setFields(creator.get(), rs)).list());
	}

	public static <T extends DataClass> @NotNull Optional<T> load(Supplier<T> creator, Map<String, Object> keys) {
		T instance = creator.get();
		String sql = buildSQL(instance.getTableName(), keys);
		return Main.database.handle(handle -> handle.createQuery(sql).bindMap(keys).map((rs, ctx) -> setFields(instance, rs)).findFirst());
	}

	public String getTableName() {
		String name = getClass().isAnnotationPresent(Table.class) ? getClass().getAnnotation(Table.class).name() : "";
		return (name.isBlank() ? getClass().getSimpleName() : name).toLowerCase();
	}

	private static Object get(Class<?> type, ResultSet rs, String name) throws SQLException {
		if (type.equals(byte.class) || type.equals(Byte.class)) return rs.getByte(name);
		if (type.equals(short.class) || type.equals(Short.class)) return rs.getShort(name);
		if (type.isAssignableFrom(int.class) || type.equals(Integer.class)) return rs.getInt(name);
		if (type.isAssignableFrom(long.class) || type.equals(Long.class)) return rs.getLong(name);
		if (type.isAssignableFrom(float.class) || type.equals(Float.class)) return rs.getFloat(name);
		if (type.isAssignableFrom(double.class) || type.equals(Double.class)) return rs.getDouble(name);
		if (type.isAssignableFrom(char.class) || type.equals(Character.class)) return rs.getString(name).charAt(0);
		if (type.isAssignableFrom(boolean.class) || type.equals(Boolean.class)) return rs.getBoolean(name);
		if (type.isAssignableFrom(String.class)) return rs.getString(name);
		return rs.getObject(name);
	}

}
