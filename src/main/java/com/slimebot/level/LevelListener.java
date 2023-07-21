package com.slimebot.level;

import com.slimebot.main.Main;
import com.slimebot.utils.MathUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LevelListener extends ListenerAdapter implements Runnable {

    public LevelListener() {
        try {
            Main.jdaInstance.awaitReady();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        //check all voice channels
        run();
        Main.scheduleAtFixedRate(30, TimeUnit.SECONDS, this);
    }

    private final static Map<Long, Long> chatTimeoutMap = new HashMap<>();
    private final static Set<Long> voiceUsers = new HashSet<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        if(chatTimeoutMap.getOrDefault(author.getIdLong(), 0L) + 30000 < System.currentTimeMillis()) {
            chatTimeoutMap.put(author.getIdLong(), System.currentTimeMillis());
            String message = event.getMessage().getContentRaw();
            String[] words = message.split(" ");
            double xp = MathUtil.randomDouble(5, 15);
            for(String word : words) {
                if(word.length() >= 3) {
                    xp += MathUtil.randomDouble(0.6, 0.8);
                }
            }
            Level.addLevel(event.getGuild().getIdLong(), author.getIdLong(), 0, MathUtil.round(xp));
        }
    }

    @Override
    public void onGenericGuildVoice(GenericGuildVoiceEvent event) {
        Member member = event.getMember();
        if(canLevel(event.getVoiceState().getChannel(), member)) voiceUsers.add(member.getIdLong());
        else voiceUsers.remove(member.getIdLong());
        System.out.println(canLevel(event.getVoiceState().getChannel(), member));
    }

    private boolean canLevel(AudioChannelUnion union, Member member) {
        return canLevel(union == null ? null : union.asVoiceChannel(), member);
    }

    private boolean canLevel(VoiceChannel channel, Member member) {
        if(channel == null) return false;
        List<Member> members = channel.getMembers();
        if(members.size() <= 1) return false;
        if(member.getVoiceState().isMuted()) return false;
        boolean flag = true, flag1 = true;
        for(Member m : members) {
            if(m.getIdLong() == member.getIdLong()) flag = false;
            else if(!m.getUser().isBot() && !m.getVoiceState().isMuted()) flag1 = false;
        }
        return !flag && !flag1;
    }

    @Override
    public void run() {
        voiceUsers.forEach(u -> Level.addLevel(0, u, 0, MathUtil.randomInt(1, 3)));
    }
}