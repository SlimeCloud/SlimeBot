package com.slimebot.games.wordchain;

import com.slimebot.games.Game;
import com.slimebot.main.Main;
import com.slimebot.games.GamePlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Wordchain extends Game<WordchainPlayer> {
    private String lastWord;
    private List<String> words = new ArrayList<>();
    private int playerTurn;
    private int round;
    private ScheduledFuture<?> scheduledFuture;
    private final short seconds;
    private final short lives;

    public Wordchain(long gameMaster, long channel, long guildId, short seconds, short lives) {
        super(gameMaster, channel, guildId, id -> new WordchainPlayer(id, lives));
        this.seconds = seconds;
        this.lives = lives;

        Main.jdaInstance.getGuildById(guildId).retrieveMemberById(gameMaster).queue(m -> {
            getChannel().sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setColor(Main.embedColor(String.valueOf(guildId)))
                                    .setTitle(m.getEffectiveName() + " hat eine neues Wortketten Spiel erstellt!")
                                    .setDescription("Um zu erfahren wie \"Wortkette\" funktioniert nutze ```/wordchain explanation```") // TODO
                                    .addField("Spielleiter:in:", "<@"+gameMaster+">", true)
                                    .addField(":timer: Timeout:", seconds + "s", true)
                                    .addField(":x: Max Fehler:", lives + " Fehler", true)
                                    .addField("Spieler:", players.stream().map(p -> ((GamePlayer)p).getAsMention()).collect(Collectors.joining("\r\n")), true)
                                    .setFooter("GameID: " + uuid)
                                    .setTimestamp(Instant.now())
                                    .build()
                    )
                    .addActionRow(
                            Button.primary(uuid + ":join", "Join"),
                            Button.danger(uuid + ":leave", "Leave"),
                            Button.primary(uuid + ":start", "Start")
                    )
                    .queue();
        });
    }

    @Override
    public void start() {
        super.start();

        sendMessage("Das Game hat begonnen! (" + uuid + ")").queue(); // TODO: better message

        round = 0;
        playerTurn = 0;

        this.lastWord = null;

        nextTurn();
    }

    private void nextTurn() {
        if(scheduledFuture != null)scheduledFuture.cancel(true);

        if(players.size() == 1) {

            // TODO: Winn stuff

            sendMessage("Spiel vorbei! " + players.get(0).getAsMention() + " hat gewonnen!").queue(); // TODO: better message
            end();
            return;
        }

        round++;
        if ((playerTurn + 1) < this.players.size()) {
            playerTurn++;
        } else playerTurn = 0;

        if(lastWord == null)sendMessage(round + ": " + this.players.get(playerTurn).getAsMention() + " ist an der Reihe! Suche dir ein Wort aus!").queue();
        if(lastWord != null)sendMessage(round + ": " + this.players.get(playerTurn).getAsMention() + " ist an der Reihe! Das letzte Wort ist \"**" + lastWord + "**\"").queue();

        scheduledFuture = Main.executor.schedule(() -> {
            damage(this.players.get(playerTurn), (p, l) -> {
                sendMessage(":x: Die Zeit von "+ p.getAsMention() + " ist abgelaufen und hat nur noch **" + l + "** Versuche!").queue();
            });
        }, seconds, TimeUnit.SECONDS);
    }

    private void damage(WordchainPlayer player, BiConsumer<WordchainPlayer, Short> messageConsumer) {
        short lives = player.lives;
        if(lives <= 1) {
            kickPlayer(player, sendMessage(":x: " + player.getAsMention() +" ist Ausgeschieden!"));
            nextTurn();
            return;
        }
        lives--;
        player.lives = lives;

        messageConsumer.accept(player, lives);
        nextTurn();
    }

    private void kickPlayer(GamePlayer player, RestAction<?> restAction) {
        restAction.queue();

        leave(player);
    }

    public MessageEmbed updateJoinEmbed(MessageEmbed embed) {
        return new EmbedBuilder()
                .setColor(Main.embedColor(String.valueOf(guildId)))
                .setTitle(embed.getTitle())
                .setDescription("Um zu erfahren wie \"Wortkette\" funktioniert nutze ```/wordchain explanation```") // TODO
                .addField("Spielleiter:in:", "<@" + gameMaster + ">", true)
                .addField(":timer: Timeout:", seconds + "s", true)
                .addField(":x: Max Fehler:", lives + " Fehler", true)
                .addField("Spieler:", players.stream().map(p -> p.getAsMention()).collect(Collectors.joining("\r\n")), true)
                .setFooter("GameID: " + uuid)
                .setTimestamp(Instant.now())
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(status != GameStatus.PLAYING) return;
        if (event.getChannel() != getChannel()) return;
        long memberID = event.getMember().getIdLong();
        if (memberID != players.get(playerTurn).id) return;

        WordchainPlayer player = players.get(playerTurn);

        String content = event.getMessage().getContentRaw();
        if(content.split(" ").length > 1) {
            event.getMessage().reply(":x: Du darfst nur 1 Wort schreiben! Versuch es noch einmal!").queue();
            return;
        }
        if(words.contains(content.toLowerCase())) {
            event.getMessage().reply(":x: Dieses Wort wurde schon genannt! Versuche es noch einmal!").queue();
            return;
        }

        // TODO: Blocked list

        // choose a word
        if(lastWord == null) {
            lastWord = content.toLowerCase();
            words.add(lastWord);
            nextTurn();
            return;
        }

        // check the word and set the new word
        char character = content.toLowerCase().charAt(0);

        if(character != Character.toLowerCase(lastWord.charAt(lastWord.length()-1))) {
            damage(player, (p, l) -> {
                sendMessage(":x: " + player.getAsMention() + " hat ein Fehler gemacht und hat nur noch **" + l + "** Versuche!").queue();
            });
            return;
        }

        lastWord = content.toLowerCase();
        words.add(lastWord);
        nextTurn();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        if(!id.startsWith(uuid.toString()))return;
        id = id.replace(uuid +":", "");

        switch(id) {
            case "join" -> {
                if (!join(event.getMember().getIdLong())) {
                    event.reply(":x: Du bist schon in einem Game!").setEphemeral(true).queue();
                    return;
                }
                event.editMessageEmbeds(updateJoinEmbed(event.getMessage().getEmbeds().get(0))).queue();

                sendMessage("<@" + event.getMember().getId() + "> ist dem Spiel beigetreten!").queue();
                return;
            }
            case "leave" -> {
                if (!players.contains(event.getMember().getIdLong())) {
                    event.reply(":x: Du bist nicht im Game!").setEphemeral(true).queue();
                    return;
                }
                if (event.getMember().getIdLong() == gameMaster) {
                    event.reply(":x: Du bist Spielleiter:in du kannst nicht verlassen!").setEphemeral(true).queue();
                    return;
                }

                leave(getPlayerFromId(event.getMember().getIdLong()));

                event.editMessageEmbeds(updateJoinEmbed(event.getMessage().getEmbeds().get(0))).queue();

                event.reply(":x: <@" + event.getMember().getId() + "> hat das Spiel verlassen!").queue();

            }
        }

        if(event.getMember().getIdLong() != gameMaster) {
            event.reply(":x: Du bist nicht Spielleiter:in!").queue();
            return;
        }
        switch(id) {
            case "start" -> {
                if (players.size() < 2) {
                    event.reply(":x: Es müssen mindestens 2 Spieler:innen mit machen!").setEphemeral(true).queue();
                    return;
                }
                event.getMessage().delete().queue();
                start();
            }
        }
    }
}
