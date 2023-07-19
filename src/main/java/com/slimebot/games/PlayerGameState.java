package com.slimebot.games;

import java.util.HashMap;
import java.util.Map;

public class PlayerGameState {
    public static Map<Long, PlayerGameState> gameStates = new HashMap<>();
    public Game game;

    public PlayerGameState(Game game) {
        this.game = game;
    }
}
