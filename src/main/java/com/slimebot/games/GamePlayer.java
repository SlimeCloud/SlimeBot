package com.slimebot.games;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class GamePlayer {
    public final long id;
    public final Game<? extends GamePlayer> game;
    public static List<GamePlayer> players = new ArrayList<>();

    protected GamePlayer(long id, Game game) {
        this.id = id;
        this.game = game;

        players.add(this);
    }

    public Optional<Member> getAsMember() {
        AtomicReference<Member> member = new AtomicReference<>();
        Objects.requireNonNull(Main.jdaInstance.getGuildById(game.guildId))
                .retrieveMemberById(id)
                .queue(member::set);
        return Optional.of(member.get());
    }

    public String getAsMention() {
        return "<@" + id + ">";
    }

    /**
     *
     * @param id
     * @return true if the player is in a game
     */
    public static boolean isInGame(long id) {
        Optional<GamePlayer> player = getFromId(GamePlayer.class, id);
        if(player.isPresent()) {
            if(player.get().game.status == Game.GameStatus.ENDED) {
                player.get().kill();
                return false;
            }
                return true;
        }
        return false;
    }

    public static <T extends GamePlayer> Optional<T> getFromId(Class<T> gamePlayerClass, long id) {
        return players.stream()
                .filter(p -> p.id == id)
                .map(gamePlayerClass::cast)
                .findAny();
    }

    /**
     * Removes the player from the game
     */
    public void kill() {
        if (game instanceof MultiPlayerGame<?>) ((MultiPlayerGame<?>) game).players.remove(this);
        players.remove(this);
    }
}
