package com.slimebot.games;

public abstract class SinglePlayerGame<T extends GamePlayer> extends Game {

    public T player;

    /**
     * @param guildId   id of the guild
     * @param channelId id of the thread Channel
     * @param player
     */
    public SinglePlayerGame(long guildId, long channelId, T player) {
        super(guildId, channelId);
        this.player = player;

        start();
    }

    public void end() {
        player.kill();
        player = null;
    }
}
