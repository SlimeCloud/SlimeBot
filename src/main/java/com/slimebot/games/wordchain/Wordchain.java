package com.slimebot.games.wordchain;

import com.slimebot.games.GamePlayer;
import com.slimebot.games.MultiPlayerGame;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Wordchain extends MultiPlayerGame<WordchainPlayer> {
    private String lastWord;
    private List<String> words = new ArrayList<>();
    private int playerTurn;
    private int round;
    private ScheduledFuture<?> scheduledFuture;
    private final short seconds;
    private final short lives;

    public Wordchain(long gameMaster, long channelId, long guildId, short seconds, short lives) {
        super(gameMaster, guildId, channelId, true, (game, id) -> new WordchainPlayer(id, lives, game));
        this.seconds = seconds;
        this.lives = lives;

        this.minPlayers = 2;
        this.maxPlayers = 10; // ????

        sendJoinMessage();
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

    @Override
    protected void messageEvent(GamePlayer gamePlayer, MessageReceivedEvent event) {
        if (!gamePlayer.equals(players.get(playerTurn))) return;

        WordchainPlayer player = (WordchainPlayer) gamePlayer;

        String content = event.getMessage().getContentRaw();
        if (content.split(" ").length > 1) {
            event.getMessage().reply(":x: Du darfst nur 1 Wort schreiben! Versuch es noch einmal!").queue();
            return;
        }
        if (words.contains(content.toLowerCase())) {
            event.getMessage().reply(":x: Dieses Wort wurde schon genannt! Versuche es noch einmal!").queue();
            return;
        }

        // TODO: Blocked list

        // choose a word
        if (lastWord == null) {
            lastWord = content.toLowerCase();
            words.add(lastWord);
            nextTurn();
            return;
        }

        // check the word and set the new word
        char character = content.toLowerCase().charAt(0);

        if (character != Character.toLowerCase(lastWord.charAt(lastWord.length() - 1))) {
            player.damage((p, l) -> {
                sendMessage(":x: " + player.getAsMention() + " hat ein Fehler gemacht und hat nur noch **" + l + "** Versuche!").queue();
            });
            return;
        }

        lastWord = content.toLowerCase();
        words.add(lastWord);
        nextTurn();
    }

    @Override
    protected EmbedBuilder buildJoinEmbed() {
        return new EmbedBuilder()
                .setColor(GuildConfig.getColor(guildId))
                .setTitle("Neues Wortketten Spiel erstellt!")
                .setDescription("Um zu erfahren wie \"Wortkette\" funktioniert nutze ```/wordchain explanation```") // TODO
                .addField("Spielleiter:in:", "<@" + gameMaster + ">", true)
                .addField(":timer: Timeout:", seconds + "s", true)
                .addField(":x: Max Fehler:", lives + " Fehler", true)
                .addField("Spieler:", players.stream().map(p -> p.getAsMention()).collect(Collectors.joining("\r\n")), true)
                .setFooter("GameID: " + uuid)
                .setTimestamp(Instant.now());
    }

    public void nextTurn() {
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
            this.players.get(playerTurn).damage((p, l) -> {
                sendMessage(":x: Die Zeit von "+ p.getAsMention() + " ist abgelaufen und hat nur noch **" + l + "** Versuche!").queue();
            });
        }, seconds, TimeUnit.SECONDS);
    }
}