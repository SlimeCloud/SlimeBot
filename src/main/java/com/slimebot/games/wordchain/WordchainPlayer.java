package com.slimebot.games.wordchain;

import com.slimebot.games.GamePlayer;

public class WordchainPlayer extends GamePlayer {
    public short lives;
    public WordchainPlayer(long id, short lives) {
        super(id);
        this.lives = lives;
    }
}
