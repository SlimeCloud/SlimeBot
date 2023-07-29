package com.slimebot.games;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class GamePlayer {
    public final long id;
    public final Game game;
    public static List<GamePlayer> players = new ArrayList<>();

    public GamePlayer(long id, Game game) {
        this.id = id;
        this.game = game;

        players.add(this);
    }

    public Member getAsMember() {
        AtomicReference<Member> member = new AtomicReference<>();
        Main.jdaInstance.getGuildById(game.guildId)
                .retrieveMemberById(id)
                .queue(m -> member.set(m));
        return member.get();
    }

    public String getAsMention() {
        return "<@" + id + ">";
    }

    /**
     * Removes the player from the game
     */
    public void kill() {
        game.players.remove(this);
        players.remove(this);
    }

    /**
     *
     * @param id
     * @return true if the player is in a game
     */
    public static boolean isInGame(long id) {
        Optional<GamePlayer> player = getFromId(id);
        if(player.isPresent()) {
            if(player.get().game.status == Game.GameStatus.ENDED) {
                player.get().kill();
                return false;
            }
                return true;
        }
        return false;
    }

    public static Optional<GamePlayer> getFromId(long id) {
        return players.stream()
                .filter(p -> p.id == id)
                .findAny();
    }
}
