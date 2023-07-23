package com.slimebot.games;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class Game <T extends GamePlayer> extends ListenerAdapter {
    public final UUID uuid = UUID.randomUUID();
    public final long channelId;
    public final long guildId;
    public long gameMaster;
    public List<T> players = new ArrayList<>();
    private final Function<Long, T> gamePlayerFunction;
    public GameStatus status;

    public Game(long gameMaster, long channelId, long guildId, Function<Long, T> gamePlayerFunction) {
        this.gameMaster = gameMaster;
        this.channelId = channelId;
        this.guildId = guildId;
        this.gamePlayerFunction = gamePlayerFunction;

        players.add(this.gamePlayerFunction.apply(gameMaster));

        PlayerGameState.setGameState(gameMaster, new PlayerGameState(this));

        status = GameStatus.WAITING;

        Main.jdaInstance.addEventListener(this);

        // End the game if its 15min WAITING
        Main.executor.schedule(() -> {
            if (status == GameStatus.WAITING) end();
        }, 15, TimeUnit.MINUTES);
    }

    public T getPlayerFromId(long id) {
        return players.stream()
                .filter(p -> p.id == id)
                .findAny()
                .orElse(null);

    }

    public boolean join(long id) {
        if (status != GameStatus.WAITING) return false;

        if (!PlayerGameState.setGameState(id, new PlayerGameState(this))) return false;
        if (getPlayerFromId(id) != null) return false;

        T player = gamePlayerFunction.apply(id);

        this.players.add(player);
        return true;
    }

    public void leave(GamePlayer player) {
        if(player == null)return;
        cleanupPlayer(player);
    }

    private void cleanupPlayer(GamePlayer player) {
        this.players.remove(player);
        if (PlayerGameState.isInGame(player.id)) PlayerGameState.releasePlayer(player.id);
    }

    public void start() {
        status = GameStatus.PLAYING;
    }

    public void end() {
        status = GameStatus.ENDED;
        Main.jdaInstance.removeEventListener(this);

        players.forEach(p -> PlayerGameState.releasePlayer(p.id));
        players = null;
    }

    public MessageChannel getChannel() {
        return Main.jdaInstance.getChannelById(MessageChannel.class, channelId);
    }

    public MessageCreateAction sendMessage(String content) {
        return getChannel().sendMessage(content);
    }

    public MessageCreateAction sendMessageEmbeds(Collection<MessageEmbed> embeds) {
        return getChannel().sendMessageEmbeds(embeds);
    }

    public enum GameStatus {
        WAITING(),
        PLAYING(),
        ENDED()
    }
}
