package de.slimecloud.slimeball.features.level;

import de.cyklon.jevent.CancellableEvent;
import de.mineking.javautils.database.Order;
import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import de.slimecloud.slimeball.config.LevelConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.MathUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface LevelTable extends Table<Level> {
	static int calculateRequiredXP(int level) {
		return (5 * level * level + 50 * level + 100);
	}

	@NotNull
	default List<Level> getTopList(@NotNull Guild guild, int offset, @Nullable Integer limit) {
		if (limit != null && limit <= 0) return Collections.emptyList();

		Stream<Level> temp = getLevels(guild).stream()
				.filter(l -> guild.getMember(l.getUser()) != null) //Ignore members who left the server
				.filter(l -> l.getLevel() > 0) //Ignore members with level 0
				.skip(offset);

		//Limit and offset according to parameters. We cannot limit and offset via SQL because we have to filter first
		if (limit != null) temp = temp.limit(limit);

		return temp.toList();
	}

	@NotNull
	default List<Level> getLevels(@NotNull Guild guild) {
		return selectMany(Where.equals("guild", guild), Order.descendingBy("level"));
	}

	@NotNull
	default Level getLevel(@NotNull Guild guild, @NotNull UserSnowflake user) {
		return selectOne(Where.allOf(
				Where.equals("user", user),
				Where.equals("guild", guild)
		)).orElseGet(() -> Level.empty(getManager().getData("bot"), guild, user));
	}

	@NotNull
	default Level getLevel(@NotNull Member user) {
		return getLevel(user.getGuild(), user);
	}

	default void reset(@NotNull Guild guild, @NotNull UserSnowflake user) {
		getLevel(guild, user).withXp(0).withLevel(0).update();
	}


	@NotNull
	default Level addLevel(@NotNull Member user, int level) {
		//Retrieve current level
		Level current = getLevel(user);

		//Call event and save if not canceled
		CancellableEvent event = new UserLevelUpEvent(user, UserGainXPEvent.Type.MANUAL, current.getXp(), current.getXp(), current.getLevel(), level);

		//Update state
		current = current.withLevel(level);

		//Call event and save if not canceled
		if (!event.callEvent()) current.update();

		return current;
	}

	@NotNull
	default Level addXp(@NotNull Member user, int xp, @NotNull UserGainXPEvent.Type type) {
		//Retrieve current level
		Level current = getLevel(user);

		//Load guild configuration
		Optional<GuildLevelConfig> config = getManager().<SlimeBot>getData("bot").loadGuild(user.getGuild()).getLevel();
		if (config.isEmpty()) return current;

		xp *= config.get().getMultiplier();
		xp += current.getXp();

		//Check for level up
		int level = current.getLevel();

		while (true) {
			int requiredXp = calculateRequiredXP(level + 1);
			if (xp < requiredXp) break;

			xp -= requiredXp;
			level++;
		}

		//Create event
		CancellableEvent event = level == current.getLevel()
				? new UserGainXPEvent(user, type, level, current.getXp(), xp)
				: new UserLevelUpEvent(user, type, current.getXp(), xp, current.getLevel(), level);

		//Update state
		current = current
				.withLevel(level)
				.withXp(xp);

		if (type == UserGainXPEvent.Type.MESSAGE) current = current.withMessages(current.getMessages() + 1);

		//Call event and save if not canceled
		if (!event.callEvent()) current.update();

		return current;
	}

	default void addMessageXp(@NotNull Member user, @NotNull String message) {
		//Load config
		Optional<LevelConfig> config = getManager().<SlimeBot>getData("bot").getConfig().getLevel();
		if (config.isEmpty()) return;

		//Calculate xp
		double xp = MathUtil.randomDouble(config.get().getMinMessageXP(), config.get().getMaxMessageXP());

		for (String word : message.split(" ")) {
			if (word.length() >= config.get().getMinWordLength()) {
				xp += MathUtil.randomDouble(config.get().getMinWordXP(), config.get().getMaxWordXP());
			}
		}

		if (xp == 0) return;

		//Add xp
		addXp(user, (int) xp, UserGainXPEvent.Type.MESSAGE);
	}

	default void addVoiceXp(@NotNull Member user) {
		//Load config
		Optional<LevelConfig> config = getManager().<SlimeBot>getData("bot").getConfig().getLevel();
		if (config.isEmpty()) return;

		//Add xp
		addXp(user, (int) MathUtil.randomDouble(config.get().getMinVoiceXP(), config.get().getMaxVoiceXP()), UserGainXPEvent.Type.VOICE);
	}
}
