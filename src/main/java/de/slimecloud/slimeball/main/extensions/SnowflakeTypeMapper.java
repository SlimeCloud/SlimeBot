package de.slimecloud.slimeball.main.extensions;

import de.mineking.databaseutils.DatabaseManager;
import de.mineking.databaseutils.TypeMapper;
import de.mineking.databaseutils.type.DataType;
import de.mineking.databaseutils.type.PostgresType;
import de.mineking.javautils.reflection.ReflectionUtils;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class SnowflakeTypeMapper implements TypeMapper<Long, ISnowflake> {
	@Override
	public boolean accepts(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field field) {
		return ISnowflake.class.isAssignableFrom(ReflectionUtils.getClass(type));
	}

	@NotNull
	@Override
	public DataType getType(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field field) {
		return PostgresType.BIG_INT;
	}

	@NotNull
	@Override
	public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field f, @Nullable Long value) {
		return new Argument() {
			@Override
			public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
				statement.setLong(position, value == null ? 0 : value);
			}

			@Override
			public String toString() {
				return Objects.toString(value);
			}
		};
	}

	@Nullable
	@Override
	public Long format(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field f, @Nullable Object value) {
		if (!(value instanceof ISnowflake snowflake)) return null;
		return snowflake.getIdLong();
	}

	@Nullable
	@Override
	public Long extract(@NotNull ResultSet set, @NotNull String name, @NotNull Type target) throws SQLException {
		return set.getLong(name);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public ISnowflake parse(@NotNull DatabaseManager manager, @NotNull Type type, @NotNull Field field, @Nullable Long value) {
		if (value == null || value == 0) return null;

		Class<?> clazz = ReflectionUtils.getClass(type);
		JDA jda = manager.<SlimeBot>getData("bot").getJda();

		if (clazz.equals(ISnowflake.class) || clazz.equals(IMentionable.class)) {
			ISnowflake temp = jda.getGuildById(value);
			if (temp != null) return temp;

			temp = jda.getRoleById(value);
			if (temp != null) return temp;

			temp = jda.getChannelById(Channel.class, value);
			if (temp != null) return temp;

			return UserSnowflake.fromId(value);
		} else if (clazz.isAssignableFrom(UserSnowflake.class)) return UserSnowflake.fromId(value);
		else if (clazz.isAssignableFrom(Guild.class)) return jda.getGuildById(value);
		else if (clazz.isAssignableFrom(Role.class)) return jda.getRoleById(value);

		else if (Channel.class.isAssignableFrom(clazz)) return jda.getChannelById((Class<? extends Channel>) type, value);

		throw new RuntimeException();
	}
}
