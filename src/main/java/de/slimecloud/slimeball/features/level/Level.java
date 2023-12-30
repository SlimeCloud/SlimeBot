package de.slimecloud.slimeball.features.level;

import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class Level implements DataClass<Level>, Comparable<Level> {
	private final SlimeBot bot;

	@Column(key = true)
	private final long guild;
	@Column(key = true)
	private final UserSnowflake user;

	@Column
	private final int level;

	@Column
	private final int xp;

	@Column
	private final int messages;

	public Level(@NotNull SlimeBot bot) {
		this(bot, 0, null, 0, 0, 0);
	}

	@NotNull
	public static Level empty(@NotNull SlimeBot bot, long guild, @NotNull UserSnowflake user) {
		return new Level(bot, guild, user, 0, 0, 0);
	}

	@NotNull
	@Override
	public Table<Level> getTable() {
		return bot.getLevel();
	}

	@NotNull
	@Override
	public Level update() {
		return (Level) DataClass.super.update();
	}

	@NotNull
	public Level withLevel(int level) {
		return new Level(bot, guild, user, level, xp, messages);
	}

	@NotNull
	public Level withXp(int xp) {
		return new Level(bot, guild, user, level, xp, messages);
	}

	@NotNull
	public Level withMessages(int messages) {
		return new Level(bot, guild, user, level, xp, messages);
	}

	public int getRank() {
		return bot.getLevel().getTopList(bot.getJda().getGuildById(guild), 0, null).stream()
				.map(l -> l.getUser().getIdLong())
				.toList().indexOf(user.getIdLong());
	}

	@Override
	public int compareTo(@NotNull Level o) {
		int levelCompare = Integer.compare(this.getLevel(), o.getLevel());
		if (levelCompare != 0) return levelCompare;

		int xpCompare = Integer.compare(this.getXp(), o.getXp());
		if (xpCompare != 0) return xpCompare;

		return Integer.compare(this.getMessages(), o.getMessages());
	}
}
