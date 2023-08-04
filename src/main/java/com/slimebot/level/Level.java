package com.slimebot.level;

import com.slimebot.database.DataClass;
import com.slimebot.database.Key;
import com.slimebot.database.Table;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.LevelGuildConfig;
import lombok.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Data
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Table(name = "levels")
public class Level extends DataClass implements Comparable<Level> {

	@Key
	private final long guild;
	@Key
	private final long user;

	private int level;
	private int xp;
	private int messages;

	public synchronized Level save() {
		super.save();
		return this;
	}

	public Optional<Integer> getRank() {
		return Optional.of(
				getTopList(guild, Integer.MAX_VALUE)
						.indexOf(this)
		).filter(i -> i != -1);
	}

	public Level addXp(int level, int xp) {
		return setXp(this.level + level, this.xp + xp);
	}

	public Level addMessages(int messages) {
		this.messages+=messages;
		return this;
	}

	public Level setXp(Integer level, Integer xp) {
		Member member = Main.jdaInstance.getGuildById(guild).getMemberById(user);

		if (level == null) level = this.level;
		if (xp == null) xp = this.xp;

		while (true) {
			int requiredXp = calculateRequiredXP(level + 1);

			if (xp < requiredXp) break;

			xp -= requiredXp;
			level++;
		}

		if (level > this.level) onLevelUp(member, level);
		this.level = level;
		this.xp = xp;
		return this;
	}

	public Level setMessages(int messages) {
		this.messages = messages;
		return this;
	}

	@Override
	public int compareTo(@NotNull Level o) {
		int levelCompare = Integer.compare(this.getLevel(), o.getLevel());
		if (levelCompare != 0) return levelCompare;

		int xpCompare = Integer.compare(this.getXp(), o.getXp());
		if (xpCompare != 0) return xpCompare;

		return Integer.compare(this.getMessages(), o.getMessages());
	}





	public static @NotNull List<Level> getTopList(long guildId, int limit) {
		if (limit <= 0) return Collections.emptyList();

		Guild guild = Main.jdaInstance.getGuildById(guildId);
		return getLevels(guildId).stream()
				.sorted(Comparator.reverseOrder())
				.filter(l -> guild.getMemberById(l.getUser()) != null)
				.limit(limit)
				.toList();
	}


	public static Level getLevel(Member member) {
		return getLevel(member.getGuild().getIdLong(), member.getIdLong());
	}

	public static List<Level> getLevels(Guild guild) {
		return guild == null ? Collections.emptyList() : getLevels(guild.getIdLong());
	}

	public static List<Level> getLevels(long guild) {
		return loadAll(() -> new Level(guild, 0), Map.of("guild", guild));
	}


	public static Level getLevel(long guild, long user) {
		return load(() -> new Level(guild, user), Map.of("guild", guild, "user", user)).orElseGet(() -> new Level(guild, user));
	}


	public static int calculateRequiredXP(int level) {
		return (5 * level * level + 50 * level + 100);
	}

	private static void onLevelUp(Member member, int newLevel) {
		if (member.getUser().isBot()) return;

		updateLevelRoles(member.getGuild().getIdLong(), member.getIdLong(), newLevel);

		GuildConfig.getConfig(member.getGuild().getIdLong()).getLevelConfig().flatMap(LevelGuildConfig::getChannel).ifPresent(
				channel -> channel.sendMessage(
						Main.config.level.levelUpMessage
								.replace("%user%", member.getAsMention())
								.replace("%level%", String.valueOf(newLevel))
				).queue()
		);
	}

	public void updateRoles() {
		updateLevelRoles(guild, user, level);
	}

	@SuppressWarnings("unchecked")
	private static void updateLevelRoles(long guildId, long userId, int level) {
		Guild guild = Main.jdaInstance.getGuildById(guildId);
		UserSnowflake user = UserSnowflake.fromId(userId);

		GuildConfig.getConfig(guild).getLevelConfig().map(config -> config.levelRoles).ifPresent(roles -> {
			Optional<Long> levelRoleId = roles.entrySet().stream()
					.filter(e -> level >= e.getKey())
					.sorted(Comparator.comparingInt(e -> ((Map.Entry<Integer, Long>) e).getKey()).reversed())
					.limit(1)
					.map(Map.Entry::getValue)
					.findAny();

			roles.forEach((l, roleId) -> {
				Role role = guild.getRoleById(roleId);

				if (levelRoleId.isPresent() && roleId.equals(levelRoleId.get())) {
					guild.addRoleToMember(user, role).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));
				} else {
					guild.removeRoleFromMember(user, role).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));
				}
			});
		});
	}
}
