package com.slimebot.level;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.LevelGuildConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public record Level(long guild, long user, int level, int xp, int messages) implements Comparable<Level> {
    public Level(long guild, long user) {
        this(guild, user, 0, 0, 0);
    }

    public static Level getLevel(long guild, long user) {
        return Main.database.handle(handle -> handle.createQuery("select * from levels where guild = :guild and \"user\" = :user")
                .bind("guild", guild)
                .bind("user", user)
                .mapTo(Level.class)
                .findOne()
                .orElseGet(() -> new Level(guild, user))
        );
    }

    public static Level getLevel(Member member) {
        return getLevel(member.getGuild().getIdLong(), member.getIdLong());
    }

    public static List<Level> getLevels(Guild guild) {
        return guild == null ? Collections.emptyList() : getLevels(guild.getIdLong());
    }

    public static List<Level> getLevels(long guild) {
        return Main.database.handle(handle -> handle.createQuery("select * from levels where guild = :guild")
                .bind("guild", guild)
                .mapTo(Level.class)
                .stream()
                .toList()
        );
    }

    public static @NotNull List<Level> getTopList(long guildId, int limit) {
        if (limit <= 0) return Collections.emptyList();

        Guild guild = Main.jdaInstance.getGuildById(guildId);

        return getLevels(guildId).stream()
                .sorted(Comparator.reverseOrder())
                .filter(l -> guild.getMemberById(l.user()) != null)
                .limit(limit)
                .toList();
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
        return new Level(guild, user, level, xp, this.messages + messages);
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

        return new Level(guild, user, level, xp, messages);
    }

    public Level setMessages(int messages) {
        return new Level(guild, user, level, xp, messages);
    }

    public Level save() {
        Main.database.run(handle -> handle.createUpdate("insert into levels values(:guild, :user, :level, :xp, :messages) on conflict(guild, \"user\") do update set level = :level, xp = :xp, messages = :messages")
                .bind("guild", guild)
                .bind("user", user)
                .bind("level", level())
                .bind("xp", xp())
                .bind("messages", messages())
                .execute()
        );

        return this;
    }

    @Override
    public int compareTo(@NotNull Level o) {
        int levelCompare = Integer.compare(this.level, o.level);

        if (levelCompare != 0) return levelCompare;

        return Integer.compare(this.xp, o.xp);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Level l && l.guild == guild && l.user == user && l.level == level && l.xp == xp && l.messages == messages();
    }

    @Override
    public int hashCode() {
        int result = (int) (guild ^ (guild >>> 32));
        result = 31 * result + (int) (user ^ (user >>> 32));
        result = 31 * result + level;
        result = 31 * result + xp;
        result = 31 * result + messages;
        return result;
    }

    @Override
    public String toString() {
        return "Level{" +
                "level=" + level +
                ", xp=" + xp +
                ", messages=" + messages +
                '}';
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

    public static class LevelMapper implements RowMapper<Level> {
        @Override
        public Level map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new Level(
                    rs.getLong("guild"),
                    rs.getLong("user"),
                    rs.getInt("level"),
                    rs.getInt("xp"),
                    rs.getInt("messages")
            );
        }
    }
}
