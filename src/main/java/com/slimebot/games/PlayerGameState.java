package com.slimebot.games;

import java.util.HashMap;
import java.util.Map;

public class PlayerGameState {
    public static Map<Long, PlayerGameState> gameStates = new HashMap<>();
    public Game game;

    public PlayerGameState(Game game) {
        this.game = game;
    }

    public static PlayerGameState getGameState(long player) {
        if(gameStates.containsKey(player))return gameStates.get(player);
        return null;
    }

    public static boolean isInGame(long player) {
        PlayerGameState state = getGameState(player);
        if(state == null)return false;
        if(state.game == null)return false;
        if(state.game.status == Game.GameStatus.ENDED) {
            releasePlayer(player);
            return false;
        }
        return true;
    }

    public static void releasePlayer(long player) {
        gameStates.remove(player);
    }

    public static boolean setGameState(long player, PlayerGameState state) {
        if(state == null)return false;
        if(isInGame(player))return false;

        gameStates.put(player, state);
        return true;
    }
}
