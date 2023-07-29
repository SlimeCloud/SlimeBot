package com.slimebot.games;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;


public abstract class Game <T extends GamePlayer> extends ListenerAdapter {
    public final UUID uuid = UUID.randomUUID();
    public long channelId;
    public final long guildId;
    public final boolean playersCanJoin;
    public short minPlayers, maxPlayers;
    public long gameMaster;
    public List<T> players = new ArrayList<>();
    public final BiFunction<Game, Long, T> gamePlayerFunction;
    public GameStatus status;
    public TimeFormat timeFormat;

    /**
     *
     * @param gameMaster memberId of the gameMaster
     * @param guildId id of the guild
     * @param playersCanJoin disables or enables the join button
     * @param gamePlayerFunction BiFunction that calls the constructor of your player class
     */
    public Game(long gameMaster, long guildId, boolean playersCanJoin, BiFunction<Game, Long, T> gamePlayerFunction) {
        this.gameMaster = gameMaster;
        this.guildId = guildId;
        this.playersCanJoin = playersCanJoin;
        this.gamePlayerFunction = gamePlayerFunction;
        this.timeFormat = TimeFormat.RELATIVE.after(15*60*1000).getFormat();

        players.add(this.gamePlayerFunction.apply(this, gameMaster));

        PlayerGameState.setGameState(gameMaster, new PlayerGameState(this));

        status = GameStatus.WAITING;

        Main.jdaInstance.addEventListener(this);

        // End the game if its 15min WAITING
        Main.executor.schedule(() -> {
            if (status == GameStatus.WAITING) end();
        }, 15, TimeUnit.MINUTES);
    }

    /**
     * Sets the ThreadChannel for this game and send the JoinEmbed built in buildJoinEmbed() in it.
     * @param action
     */

    public void sendJoinMessageInThread(ThreadChannelAction action) {
        action.flatMap(channel -> {
            this.channelId = channel.getIdLong();
            return sendMessage(buildJoinMessage());
        }).queue();
    }

    private MessageCreateData buildJoinMessage() {
        return new MessageCreateBuilder().setEmbeds(buildJoinEmbed()
                .addField(":alarm_clock: Automatisch Löschen", timeFormat.toString(), true)
                .build())
                .setActionRow(
                        Button.primary(uuid + ":join", "Join").withDisabled(!playersCanJoin),
                        Button.danger(uuid + ":leave", "Leave"),
                        Button.primary(uuid + ":start", "Start")
                ).build();
    }

    /**
     *
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

        if(GamePlayer.isInGame(id))return false;

        T player = gamePlayerFunction.apply(this, id);

        this.players.add(player);
        return true;
    }

    public void start() {
        status = GameStatus.PLAYING;
    }

    /**
     * Important to call in the end of the game!
     * Kills all players and removes eventListener
     */
    public void end() {
        status = GameStatus.ENDED;
        Main.jdaInstance.removeEventListener(this);

        players.forEach(p -> p.kill());
        players = null;

        if(getChannel() != null) getChannel().delete().queueAfter(3, TimeUnit.MINUTES);
    }

    public ThreadChannel getChannel() {
        return Main.jdaInstance.getChannelById(ThreadChannel.class, channelId);
    }

    public MessageCreateAction sendMessage(String content) {
        return getChannel().sendMessage(content);
    }

    public MessageCreateAction sendMessage(MessageCreateData message) {
        return getChannel().sendMessage(message);
    }

    public MessageCreateAction sendMessageEmbeds(Collection<MessageEmbed> embeds) {
        return getChannel().sendMessageEmbeds(embeds);
    }

    public MessageCreateAction sendMessageEmbeds(MessageEmbed embed) {
        return getChannel().sendMessageEmbeds(embed);
    }

    protected abstract EmbedBuilder buildJoinEmbed();

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        if(!id.startsWith(uuid.toString()))return;
        id = id.replace(uuid +":", "");

        switch(id) {
            case "join" -> {
                if(players.size() >= maxPlayers) {
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

        if(event.getMember().getIdLong() != gameMaster) {
            event.reply(":x: Du bist nicht Spielleiter:in!").queue();
            return;
        }
        switch(id) {
            case "start" -> {
                if (players.size() < minPlayers) {
                    event.reply(":x: Es müssen mindestens " + minPlayers + " Spieler:innen mit machen!").setEphemeral(true).queue();
                    return;
                }
                event.editComponents(event.getMessage().getComponents().stream().map(c -> c.asDisabled()).toList()).queue();
                start();
            }
        }
    }

    public enum GameStatus {
        WAITING(),
        PLAYING(),
        ENDED()
    }
}