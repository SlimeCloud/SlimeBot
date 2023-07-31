package com.slimebot.games;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public abstract class Game <T extends GamePlayer> extends ListenerAdapter {
    public final UUID uuid = UUID.randomUUID();
    public long channelId;
    public final long guildId;
    public short minPlayers;
    public short maxPlayers;
    public GameStatus status;

    /**
     * @param guildId id of the guild
     */
    protected Game(long guildId, long channelId) {
        this.guildId = guildId;
        this.channelId = channelId;

        status = GameStatus.WAITING;

        Main.jdaInstance.addEventListener(this);

        // End the game if its 15min WAITING
        Main.executor.schedule(() -> {
            if (status == GameStatus.WAITING) end();
        }, 15, TimeUnit.MINUTES);
    }

    public void start() {
        status = GameStatus.PLAYING;
    }

    /**
     * Important to call in the end of the game!
     * Kills all players and removes eventListener
     */
    public void end() {
        status = GameStatus.ENDED;
        Main.jdaInstance.removeEventListener(this);

        if(getChannel() != null) getChannel().delete().queueAfter(3, TimeUnit.MINUTES);
    }

    public ThreadChannel getChannel() {
        return Main.jdaInstance.getChannelById(ThreadChannel.class, channelId);
    }

    public MessageCreateAction sendMessage(String content) {
        return getChannel().sendMessage(content);
    }

    public MessageCreateAction sendMessage(MessageCreateData message) {
        return getChannel().sendMessage(message);
    }

    public MessageCreateAction sendMessageEmbeds(Collection<MessageEmbed> embeds) {
        return getChannel().sendMessageEmbeds(embeds);
    }

    public MessageCreateAction sendMessageEmbeds(MessageEmbed embed) {
        return getChannel().sendMessageEmbeds(embed);
    }

    protected abstract void messageEvent(GamePlayer player, MessageReceivedEvent event);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (status != GameStatus.PLAYING) return;
        if (event.getChannel().getIdLong() != channelId) return;
        GamePlayer.getFromId(GamePlayer.class, event.getMember().getIdLong()).ifPresentOrElse(p -> {
            if (p.game == this) messageEvent(p, event);
            else event.getMessage().delete();
        }, () -> event.getMessage().delete().queue());
    }

    public enum GameStatus {
        WAITING(),
        PLAYING(),
        ENDED()
    }
}