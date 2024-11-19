package de.slimecloud.slimeball.features.level;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class Level implements DataClass<Level>, Comparable<Level>, ListEntry {
	private final transient SlimeBot bot;

	@Column(key = true)
	private transient final Guild guild;
	@Column(key = true)
	private transient final UserSnowflake user;

	@Column
	private final int level;

	@Column
	private final int xp;

	@Column
	private final int messages;

	public Level(@NotNull SlimeBot bot) {
		this(bot, null, null, 0, 0, 0);
	}

	@NotNull
	public static Level empty(@NotNull SlimeBot bot, @NotNull Guild guild, @NotNull UserSnowflake user) {
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
		return DataClass.super.update();
	}

	public int getTotalXp() {
		int result = 0;
		for (int i = 1; i <= level; i++) result += LevelTable.getRequiredXp(i);
		return result + xp;
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
		return bot.getLevel().getTopList(guild, 0, null).stream()
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

	@NotNull
	@Override
	public String build(int index, @NotNull ListContext<? extends ListEntry> context) {
		return null;
	}
}
