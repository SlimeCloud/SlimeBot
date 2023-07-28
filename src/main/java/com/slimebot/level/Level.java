package com.slimebot.level;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.LevelGuildConfig;
import net.dv8tion.jda.api.entities.Member;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

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

    public static List<Level> getLevels(long guild) {
        return Main.database.handle(handle -> handle.createQuery("select * from levels where guild = :guild")
                .bind("guild", guild)
                .mapTo(Level.class)
                .stream()
                .filter(level -> !Main.jdaInstance.getUserById(level.user()).isBot())
                .toList()
        );
    }

    public static @NotNull List<Level> getTopList(long guild, int limit) {
        if (limit <= 0) return Collections.emptyList();

        return getLevels(guild).stream()
                .sorted()
                .limit(limit)
                .toList();
    }

    public Level addXp(int level, int xp) {
        Member member = Main.jdaInstance.getGuildById(guild).getMemberById(user);

        System.out.println(member + " + " + level + "," + xp);

        int newLevel = this.level + level;
        int newXp = this.xp + xp;

        while(true) {
            int requiredXp = calculateRequiredXP(level + 1);

            if(newXp < requiredXp) break;

            newXp -= requiredXp;
            level++;

            onLevelUp(member, level);
        }

        return new Level(guild, user, newLevel, newXp, messages);
    }

    public Level addMessages(int messages) {
        return new Level(guild, user, level, xp, this.messages + messages);
    }

    public Level setXp(Integer level, Integer xp) {
        return new Level(guild, user, level == null ? this.level : level, xp == null ? this.xp : xp, messages);
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

        if(levelCompare != 0) return levelCompare;

        return Integer.compare(this.xp, o.xp);
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
        return (5 * level*level + 50 * level + 100);
    }

    private static void onLevelUp(Member member, int newLevel) {
        if(member.getUser().isBot()) return;

        GuildConfig.getConfig(member.getGuild().getIdLong()).getLevelConfig().flatMap(LevelGuildConfig::getChannel).ifPresent(
                channel -> channel.sendMessage(
                        Main.config.level.levelUpMessage
                                .replace("%user%", member.getAsMention())
                                .replace("%level%", String.valueOf(newLevel))
                ).queue()
        );
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
