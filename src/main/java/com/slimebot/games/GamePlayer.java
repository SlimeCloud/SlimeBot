package com.slimebot.games;

public abstract class GamePlayer {
    public final long id;

    public GamePlayer(long id) {
        this.id = id;
    }

    public String getAsMention() {
        return "<@" + id + ">";
    }
}
