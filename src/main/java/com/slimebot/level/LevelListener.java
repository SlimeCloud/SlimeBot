package com.slimebot.level;

import com.slimebot.main.Main;
import com.slimebot.main.config.LevelConfig;
import com.slimebot.util.MathUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LevelListener extends ListenerAdapter implements Runnable {

    private final static LevelConfig config = Main.config.level;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        //check all voice channels
        Main.jdaInstance.getGuilds().forEach(guild -> guild.getVoiceChannels().forEach(voice -> voice.getMembers().forEach(member -> testLeveling(voice, guild, member))));
        run();
        Main.scheduleAtFixedRate(30, TimeUnit.SECONDS, this);
    }

    private final static Map<Long, Long> chatTimeoutMap = new HashMap<>();
    private final static Map<Long, Long> voiceUsers = new HashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        if(chatTimeoutMap.getOrDefault(author.getIdLong(), 0L) + config.messageCooldown >= System.currentTimeMillis()) return;

        chatTimeoutMap.put(author.getIdLong(), System.currentTimeMillis());
        String message = event.getMessage().getContentRaw();
        String[] words = message.split(" ");
        double xp = MathUtil.randomInt(config.minMessageXP, config.maxMessageXP);
        for(String word : words) {
            if(word.length() >= config.minWordLength) {
                xp += MathUtil.randomDouble(config.minWordXP, config.maxWordXP);
            }
        }
        Level.addLevel(event.getGuild().getIdLong(), author.getIdLong(), 0, MathUtil.round(xp));

    }

    @Override
    public void onGenericGuildVoice(GenericGuildVoiceEvent event) {
        AudioChannelUnion union = event.getVoiceState().getChannel();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        testLeveling(union, guild, member);
        if (union!=null) union.asVoiceChannel().getMembers().forEach(m -> testLeveling(union, guild, m));
    }

    private void testLeveling(VoiceChannel voice, Guild guild, Member member) {
        if(canLevel(voice, member)) voiceUsers.put(member.getIdLong(), guild.getIdLong());
        else voiceUsers.remove(member.getIdLong());
    }

    private void testLeveling(AudioChannelUnion union, Guild guild, Member member) {
        if(canLevel(union, member)) voiceUsers.put(member.getIdLong(), guild.getIdLong());
        else voiceUsers.remove(member.getIdLong());
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
        voiceUsers.forEach((k, v) -> Level.addLevel(v, k, 0, MathUtil.randomInt(config.minVoiceXP, config.maxVoiceXP)));
    }
}