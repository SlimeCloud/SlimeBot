package com.slimebot.level;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.util.MathUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public record Level(long guildId, long userId, int level, int xp) implements Comparable<Level> {

    public void save() {
        Main.database.run(handle -> handle.createUpdate("insert into levels values(:guild, :user, :level, :xp) on conflict(guild, \"user\") do update set level = :level, xp = :xp")
                .bind("guild", guildId())
                .bind("user", userId())
                .bind("level", level())
                .bind("xp", xp())
                .execute());
    }

    public Level add(int level, int xp) {
        return new Level(guildId(), userId(), level()+level, xp()+xp);
    }

    @Override
    public int compareTo(@NotNull Level o) {
        int levelComp = Integer.compare(o.level, this.level);
        return levelComp != 0 ? levelComp : Double.compare(o.xp, this.xp);
    }

    @Override
    public String toString() {
        return String.format("%s {guildId: %s, userId: %s, level: %s, xp: %s}", getClass(), guildId(), userId(), level(), xp());
    }

    public static class LevelMapper implements RowMapper<Level> {

        @Override
        public Level map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new Level(
                    rs.getLong("guild"),
                    rs.getLong("user"),
                    rs.getInt("level"),
                    rs.getInt("xp")
            );
        }
    }

    public static Level load(long guildId, long userId) {
        return Main.database.handle(handle -> handle.createQuery("select * from levels where guild = :guild and \"user\" = :user")
                .bind("guild", guildId)
                .bind("user", userId)
                .mapTo(Level.class)
                .findOne()
                .orElseGet(() -> new Level(guildId, userId, 0, 0))
        );
    }

    public static int calculateRequiredXP(int level) {
        return (int) (5*Math.pow(level, 2)+(50*level)+100);
    }

    public static void addUser(long guildId, long userId) {
        if(getLevel(guildId, userId) != null) return;
        new Level(guildId, userId, 0, 0).save();
    }

    public static Level getLevel(long guildId, long userId) {
        return load(guildId, userId);
    }

    public static void setLevel(Level level) {
        setLevel(level.guildId(), level.userId(), level.level(), level.xp());
    }

    public static void setLevel(long guildId, long userId, int level, int xp) {
        Member member = Main.jdaInstance.getGuildById(guildId).getMemberById(userId);

        int reqXp = calculateRequiredXP(level+1);
        while (reqXp<=xp) {
            onLevelUp(member.getGuild(), member, level, ++level, xp, xp-=reqXp);
            reqXp = calculateRequiredXP(level+1);
        }
        new Level(guildId, userId, level, xp).save();
    }

    public static void addLevel(long guildId, long userId, int level, int xp) {
        setLevel(getLevel(guildId, userId).add(level, MathUtil.round(xp*Main.config.level.xpMultiplier)));
    }

    public static List<Level> getLevels(long guildId) {
        return Main.database.handle(handle -> handle.createQuery("select * from levels where guild = :guild")
                .bind("guild", guildId)
                .mapTo(Level.class)
                .stream()
                .filter(level -> !Main.jdaInstance.getUserById(level.userId()).isBot())
                .toList()
        );
    }

    public static List<Level> getTop(long guildId, int limit) {
        List<Level> data = new ArrayList<>();
        if(limit <= 0) return data;
        data.addAll(getLevels(guildId));
        data.sort(null);
        return data.subList(0, Math.max(1, Math.min(data.size(), limit)));
    }

    public static int getPosition(long guildId, long userId) {
        List<Level> data = getTop(guildId, 0);
        for(int i = 0; i < data.size(); i++) {
            if(data.get(i).userId() == userId) return i + 1;
        }
        throw new RuntimeException();
    }

    private static void onLevelUp(Guild guild, Member member, int oldLevel, int newLevel, int oldXp, int newXp) {
        TextChannel channel = Main.jdaInstance.getTextChannelById(GuildConfig.getConfig(guild.getIdLong()).level.notificationChannel);
        if (channel==null) return;
        channel.sendMessage(Main.config.level.levelUpMessage.replace("%user%", member.getAsMention()).replace("%level%", String.valueOf(newLevel))).queue();
    }
}
