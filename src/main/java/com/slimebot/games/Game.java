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
    public long gameMaster;
    public List<Long> player;
    public GameStatus status;
    public final MessageChannel channel;
    public final long guildId;

    public Game(long gameMaster, MessageChannel channel, long guildId) {
        this.gameMaster = gameMaster;
        this.channel = channel;
        this.guildId = guildId;
        this.uuid = UUID.randomUUID();

        player = new ArrayList<>();
        player.add(gameMaster);

        PlayerGameState.setGameState(gameMaster, new PlayerGameState(this));

        status = GameStatus.CREATING;

        Main.jdaInstance.addEventListener(this);

        // End the game if its 15min WAITING or CREATING
        Main.executor.schedule(() -> {
            if(status == GameStatus.CREATING ||status == GameStatus.WAITING)end();
        }, 15, TimeUnit.MINUTES);
    }

    public void create() {
        if(status != GameStatus.CREATING)return;
        status = GameStatus.WAITING;
    }

    public boolean join(long player) {
        if(status != GameStatus.WAITING)return false;

        if(this.player.contains(player))return false;
        if(!PlayerGameState.setGameState(player, new PlayerGameState(this)))return false;

        this.player.add(player);
        return true;
    }

    public void leave(long player) {
        cleanupPlayer(player);
    }

    private void cleanupPlayer(long player) {
        if(this.player.contains(player))this.player.remove(player);
        if(PlayerGameState.isInGame(player))PlayerGameState.releasePlayer(player);
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

    public enum GameStatus {
        CREATING(),
        WAITING(),
        PLAYING(),
        ENDED();
    }
}
