package com.slimebot.games.wordchain;

import com.slimebot.games.Game;
import com.slimebot.games.GamePlayer;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.function.BiConsumer;

public class WordchainPlayer extends GamePlayer {
    public short lives;
    public WordchainPlayer(long id, short lives, Game game) {
        super(id, game);
        this.lives = lives;
    }

    public void damage(BiConsumer<WordchainPlayer, Short> messageConsumer) {
        if(lives <= 1) {
            this.game.sendMessage(":x: " + getAsMention() +" ist Ausgeschieden!").queue();
            kick();
            ((Wordchain) this.game).nextTurn();
            return;
        }
        lives--;

        messageConsumer.accept(this, lives);

        ((Wordchain) this.game).nextTurn();
    }

    public void kick() {
        kill();
    }
}
