package com.slimebot.level;

import com.slimebot.main.Main;
import com.slimebot.utils.MathUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record Level(long guildId, long userId, int level, int xp) implements Comparable<Level> {


    public static void main(String[] args) {
        for(int i = 0; i <= 32; i++) {
            System.out.println(i + " -> " + calculateRequiredXP(i));
        }
    }


    @Override
    public int compareTo(@NotNull Level o) {
        int levelComp = Integer.compare(this.level, o.level);
        return levelComp != 0 ? levelComp : Double.compare(this.xp, o.xp);
    }

    public static int calculateRequiredXP(int level) {
        return (int) (5*Math.pow(level, 2)+(50*level)+100);
    }

    public static void addUser(long guildId, long userId) {
        if(getLevel(guildId, userId) != null) return;
        new Level(guildId, userId, 0, 0); //TODO add this Data to DB
    }

    private static int lvl = 0;

    public static Level getLevel(long guildId, long userId) {
        return new Level(guildId, userId, lvl += 8, 0); //TODO read Level from DB
    }

    public static void addLevel(long guildId, long userId, int level, int xp) {
        int reqXp = calculateRequiredXP(getLevel(guildId, userId).level() + 1);
        while(reqXp <= xp) {
            addLevel(guildId, userId, 1, 0);
            xp -= reqXp;
            reqXp = calculateRequiredXP(getLevel(guildId, userId).level() + 1);
        }
        //add level and remaining xp
        //TODO update in DB
    }

    public static List<Level> getLevels(long guildId) {
        //TODO Replace with DB Request
        Guild guild = Main.jdaInstance.getGuildById(guildId);
        List<Level> result = new ArrayList<>();
        if(guild == null) return result;
        guild.getMembers().forEach(m -> result.add(getLevel(guildId, m.getIdLong())));
        result.removeIf(Objects::isNull);
        return result;
    }

    public static List<Level> getTop(long guildId, int limit) {
        List<Level> data = getLevels(guildId);
        Collections.sort(data);
        if(limit <= 0) return data;
        return data.subList(0, limit - 1);
    }

    public static int getPosition(long guildId, long userId) {
        addUser(guildId, userId);
        List<Level> data = getTop(guildId, 0);
        for(int i = 0; i < data.size(); i++) {
            if(data.get(i).userId() == userId) return i + 1;
        }
        return 0; //Impossible
    }
}
