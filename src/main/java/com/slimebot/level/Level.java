package com.slimebot.level;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record Level(String guildId, String userId, int level, double xp) implements Comparable<Level> {

    private static final double BASE_XP = 203.5;
    private static final double GROWTH_FACTOR = 1.8;


    @Override
    public int compareTo(@NotNull Level o) {
        int levelComp = Integer.compare(this.level, o.level);
        return levelComp!=0 ? levelComp : Double.compare(this.xp, o.xp);
    }

    public static double calculateRequiredXP(int level) {
        return (BASE_XP*Math.pow(GROWTH_FACTOR, level-1));
    }

    public static void addUser(String guildId, String userId) {
        if (getLevel(guildId, userId)!=null) return;
        new Level(guildId, userId, 0, 0); //TODO add this Data to DB
    }

    public static Level getLevel(String guildId, String userId) {
        return new Level(guildId, userId, 0, 0); //TODO read Level from DB
    }

    public static List<Level> getLevels(String guildId) {
        Guild guild = Main.jdaInstance.getGuildById(guildId);
        List<Level> result = new ArrayList<>();
        if (guild==null) return result;
        guild.getMembers().forEach((m) -> result.add(getLevel(guildId, m.getId())));
        result.removeIf(Objects::isNull);
        return result;
    }

    public static List<Level> getTop(String guildId, int limit) {
        List<Level> data = getLevels(guildId);
        Collections.sort(data);
        if (limit<=0) return data;
        return data.subList(0, limit-1);
    }

    public static int getPosition(String guildId, String userId) {
        addUser(guildId, userId);
        List<Level> data = getTop(guildId, 0);
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).userId().equals(userId)) return i+1;
        }
        return 0; //Impossible
    }

}
