package com.slimebot.games.games;

import com.slimebot.games.Game;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Wordchain extends Game {
    private Character character;
    private int playerTurn;
    private int round;
    private ScheduledFuture<?> scheduledFuture;
    private final short seconds;
    private final short lives;
    private Map<Long, Short> playerLives;

    public Wordchain(long gameMaster, long channel, long guildId, short seconds, short lives) {
        super(gameMaster, channel, guildId);
        this.playerLives = new HashMap<>();
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
                                    .addField("Spieler:", player.stream().map(p -> ("<@"+ p + ">")).collect(Collectors.joining("\r\n")), true)
                                    .setFooter("GameID: " + uuid)
                                    .setTimestamp(Instant.now())
                                    .build()
                    )
                    .addActionRow(
                            Button.primary(uuid + ":join", "Join"),
                            Button.primary(uuid + ":start", "Start")
                    )
                    .queue();
        });
    }



    @Override
    public void start() {
        super.start();

        getChannel().sendMessage("Das Game hat begonnen! (" + uuid + ")").queue();

        player.forEach(p -> playerLives.put(p, lives));

        round = 0;
        playerTurn = 0;

        nextTurn(null);
    }

    private void nextTurn(String lastWord) {
        if(scheduledFuture != null)scheduledFuture.cancel(true);

        if(player.size() == 1) {
            // TODO: Winn stuff
            getChannel().sendMessage("Spiel vorbei! blablabla").queue(); // TODO: better message
            end();
            return;
        }

        round++;
        if((playerTurn+1) < this.player.size()) {
            playerTurn++;
        }else {
            playerTurn = 0;
        }

        if(lastWord == null)getChannel().sendMessage(round + ": <@" + this.player.get(playerTurn) + "> ist an der Reihe! Suche dir ein Wort aus!").queue();
        if(lastWord != null)getChannel().sendMessage(round + ": <@" + this.player.get(playerTurn) + "> ist an der Reihe! Das letzte Wort ist \"**" + lastWord + "**\"").queue();

        scheduledFuture = Main.executor.schedule(() -> {
            kickPlayer(this.player.get(playerTurn), getChannel().sendMessage(":x: <@" + player +"> ist Ausgeschieden weil die Zeit abgelaufen ist!"));
            nextTurn(lastWord);
        }, seconds, TimeUnit.SECONDS);
    }

    private void damage(long player) {
        short lives = playerLives.get(player);
        if(lives <= 1) {
            kickPlayer(player, getChannel().sendMessage(":x: <@" + player +"> ist Ausgeschieden!"));
            playerLives.remove(player);
            return;
        }
        lives--;
        playerLives.put(player, lives);

        getChannel().sendMessage(":x: <@" + player +"> hat ein Fehler gemacht und hat nur noch **"+ lives+"** Versuche!").queue();
    }

    private void kickPlayer(long player, RestAction<?> restAction) {
        restAction.queue();

        leave(player);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getChannel() != getChannel())return;
        long memberID = event.getMember().getIdLong();
        if(memberID != player.get(playerTurn))return;

        String content = event.getMessage().getContentRaw();
        String lastWord = null;

        if(character != null) {
            if(content.split(" ").length > 1 || Character.toLowerCase(content.toCharArray()[0]) != Character.toLowerCase(character)) {
                damage(memberID);
            }else {
                lastWord = content.split(" ")[0];
            }
        }

        character = content.toCharArray()[content.length()-1];
        nextTurn(lastWord);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        if(!id.startsWith(uuid.toString()))return;
        id = id.replace(uuid +":", "");

        switch(id) {
            case "join":
                if(!join(event.getMember().getIdLong())) {
                    event.reply(":x: Du bist schon in einem Game!").setEphemeral(true).queue(); // TODO: better message
                    return;
                }
                event.editMessageEmbeds(
                        new EmbedBuilder()
                                .setColor(Main.embedColor(String.valueOf(guildId)))
                                .setTitle(event.getMessage().getEmbeds().get(0).getTitle())
                                .setDescription("Um zu erfahren wie \"Wortkette\" funktioniert nutze ```/wordchain explanation```") // TODO
                                .addField("Spielleiter:in:", "<@"+gameMaster+">", true)
                                .addField(":timer: Timeout:", seconds + "s", true)
                                .addField(":x: Max Fehler:", lives + " Fehler", true)
                                .addField("Spieler:", player.stream().map(p -> ("<@"+ p + ">")).collect(Collectors.joining("\r\n")), true)
                                .setFooter("GameID: " + uuid)
                                .setTimestamp(Instant.now())
                                .build()
                ).queue();
                event.reply("<@"+ event.getMember().getId()+"> ist dem Spiel beigetreten!").queue(); // TODO: better message
                return;
        }

        if(event.getMember().getIdLong() != gameMaster) {
            event.reply(":x: Du bist nicht Spielleiter:in! uiashdfuihwesuiafgh").queue(); // TODO: better message
            return;
        }
        switch(id) {
            case "start":
                if(player.size() >= 2) {
                    event.getMessage().delete().queue();
                    start();
                }else {
                    event.reply(":x: Es müssen mindestens 2 Spieler:innen mit machen!").setEphemeral(true).queue();
                }
        }
    }
}
