package com.slimebot.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class MultiPlayerGame<T extends GamePlayer> extends Game {

    public final long gameMaster;
    public final boolean playersCanJoin;
    private final BiFunction<Game, Long, T> gamePlayerFunction;
    public List<T> players = new ArrayList<>();


    /**
     * @param gameMaster         memberId of the gameMaster
     * @param guildId            id of the guild
     * @param channelId          id of the thread Channel
     * @param playersCanJoin     disables or enables the join button
     * @param gamePlayerFunction BiFunction that calls the constructor of your player class
     */
    protected MultiPlayerGame(long gameMaster, long guildId, long channelId, boolean playersCanJoin, BiFunction<Game, Long, T> gamePlayerFunction) {
        super(guildId, channelId);

        this.gameMaster = gameMaster;
        this.playersCanJoin = playersCanJoin;
        this.gamePlayerFunction = gamePlayerFunction;

        players.add(this.gamePlayerFunction.apply(this, gameMaster));
    }

    public void sendJoinMessage() {
        sendMessage(buildJoinMessage()).queue();
    }

    private MessageCreateData buildJoinMessage() {
        return new MessageCreateBuilder().setEmbeds(buildJoinEmbed()
                        .addField(":alarm_clock: Automatisch Löschen", TimeFormat.RELATIVE.after(15 * 60 * 1000).toString(), true)
                        .build())
                .setActionRow(
                        Button.primary(uuid + ":join", "Join").withDisabled(!playersCanJoin),
                        Button.danger(uuid + ":leave", "Leave"),
                        Button.primary(uuid + ":start", "Start")
                ).build();
    }

    /**
     * @param id
     * @return Optional player
     */
    public Optional<T> getPlayerFromId(long id) {
        return players.stream()
                .filter(p -> p.id == id)
                .findAny();
    }

    public boolean join(long id) {
        if (status != GameStatus.WAITING) return false;

        if (GamePlayer.isInGame(id)) return false;

        T player = gamePlayerFunction.apply(this, id);

        this.players.add(player);
        return true;
    }

    public void end() {
        super.end();
        players.forEach(p -> p.kill());
        players = null;
    }

    protected abstract EmbedBuilder buildJoinEmbed();

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        if (!id.startsWith(uuid.toString())) return;
        id = id.replace(uuid + ":", "");

        switch (id) {
            case "join" -> {
                if (players.size() >= maxPlayers) {
                    event.reply(":x: Das Spiel ist bereits voll!").setEphemeral(true).queue();
                    return;
                }
                if (!join(event.getMember().getIdLong())) {
                    event.reply(":x: Du bist schon in einem Game!").setEphemeral(true).queue();
                    return;
                }
                event.editMessageEmbeds(buildJoinMessage().getEmbeds()).queue();

                sendMessage(event.getMember().getAsMention() + " ist dem Spiel beigetreten!").queue();
                return;
            }
            case "leave" -> {
                if (getPlayerFromId(event.getMember().getIdLong()).isEmpty()) {
                    event.reply(":x: Du bist nicht im Game!").setEphemeral(true).queue();
                    return;
                }
                if (event.getMember().getIdLong() == gameMaster) {
                    event.reply(":x: Du bist Spielleiter:in du kannst nicht verlassen!").setEphemeral(true).queue();
                    return;
                }

                getPlayerFromId(event.getMember().getIdLong()).ifPresent(p -> p.kill());

                event.editMessageEmbeds(buildJoinMessage().getEmbeds()).queue();

                event.reply(":x: " + event.getMember().getAsMention() + " hat das Spiel verlassen!").queue();

            }
        }

        if (event.getMember().getIdLong() != gameMaster) {
            event.reply(":x: Du bist nicht Spielleiter:in!").queue();
            return;
        }
        if (id.equals("start")) {
            if (players.size() < minPlayers) {
                event.reply(":x: Es müssen mindestens " + minPlayers + " Spieler:innen mit machen!").setEphemeral(true).queue();
                return;
            }
            event.editComponents(event.getMessage().getComponents().stream().map(c -> c.asDisabled()).toList()).queue();
            start();
        }
    }
}
