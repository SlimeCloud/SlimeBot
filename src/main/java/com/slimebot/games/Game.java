package com.slimebot.games;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class Game extends ListenerAdapter {
    public long gameMaster;
    public List<Long> player;
    public GameStatus status;
    public MessageChannel channel;

    public Game(long gameMaster, MessageChannel channel) {
        this.gameMaster = gameMaster;
        this.channel = channel;

        player = new ArrayList<>();
        player.add(gameMaster);

        status = GameStatus.WAITING;

        Main.jdaInstance.addEventListener(this);

        // End the game if its 10min WAITING
        Main.executor.schedule(() -> {
            if(status == GameStatus.WAITING)end();
        }, 10, TimeUnit.MINUTES);
    }

    public boolean join(long player) {
        if(status != GameStatus.WAITING)return false;
        if(PlayerGameState.gameStates.containsKey(player)) {
            if(PlayerGameState.gameStates.get(player).game.status != GameStatus.ENDED)return false;
            PlayerGameState.gameStates.remove(player);
        }
        if(this.player.contains(player))return false;
        PlayerGameState.gameStates.put(player, new PlayerGameState(this));

        this.player.add(player);
        return true;
    }

    public void leave(long player) {
        cleanupPlayer(player);
    }

    public void cleanupPlayer(long player) {
        if(this.player.contains(player))this.player.remove(player);
        if(PlayerGameState.gameStates.containsKey(player))PlayerGameState.gameStates.remove(player);
    }

    public void start() {
        status = GameStatus.PLAYING;
    }

    public void end() {
        status = GameStatus.ENDED;
        Main.jdaInstance.removeEventListener(this);

        player.forEach(this::cleanupPlayer);
    }

    public enum GameStatus {
        WAITING(),
        PLAYING(),
        ENDED();
    }
}
