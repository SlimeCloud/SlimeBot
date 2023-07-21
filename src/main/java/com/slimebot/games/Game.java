package com.slimebot.games;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class Game extends ListenerAdapter {

    public final UUID uuid;
    public final long channelId;
    public final long guildId;
    public long gameMaster;
    public List<Long> player;
    public GameStatus status;

    public Game(long gameMaster, long channelId, long guildId) {
        this.gameMaster = gameMaster;
        this.channelId = channelId;
        this.guildId = guildId;
        this.uuid = UUID.randomUUID();

        player = new ArrayList<>();
        player.add(gameMaster);

        PlayerGameState.setGameState(gameMaster, new PlayerGameState(this));

        status = GameStatus.WAITING;

        Main.jdaInstance.addEventListener(this);

        // End the game if its 15min WAITING
        Main.executor.schedule(() -> {
            if (status == GameStatus.WAITING) end();
        }, 15, TimeUnit.MINUTES);
    }

    public boolean join(long player) {
        if (status != GameStatus.WAITING) return false;

        if (this.player.contains(player)) return false;
        if (!PlayerGameState.setGameState(player, new PlayerGameState(this))) return false;

        this.player.add(player);
        return true;
    }

    public void leave(long player) {
        cleanupPlayer(player);
    }

    private void cleanupPlayer(long player) {
        this.player.remove(player);
        if (PlayerGameState.isInGame(player)) PlayerGameState.releasePlayer(player);
    }

    public void start() {
        status = GameStatus.PLAYING;
    }

    public void end() {
        status = GameStatus.ENDED;
        Main.jdaInstance.removeEventListener(this);

        player.forEach(p -> PlayerGameState.releasePlayer(p));
        player = null;
    }

    public MessageChannel getChannel() {
        return Main.jdaInstance.getChannelById(MessageChannel.class, channelId);
    }

    public enum GameStatus {
        WAITING(),
        PLAYING(),
        ENDED()
    }
}
