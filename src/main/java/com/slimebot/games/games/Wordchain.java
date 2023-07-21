package com.slimebot.games.games;

import com.slimebot.games.Game;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Wordchain extends Game {
    private String lastWord;
    private List<String> words;
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
                            Button.danger(uuid + ":leave", "Leave"),
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

        this.lastWord = null;

        nextTurn();
    }

    private void nextTurn() {
        if(scheduledFuture != null)scheduledFuture.cancel(true);

        if(player.size() == 1) {
            // TODO: Winn stuff
            getChannel().sendMessage("Spiel vorbei! blablabla").queue(); // TODO: better message
            end();
            return;
        }

        round++;
        if ((playerTurn + 1) < this.player.size()) {
            playerTurn++;
        } else playerTurn = 0;

        if(lastWord == null)getChannel().sendMessage(round + ": <@" + this.player.get(playerTurn) + "> ist an der Reihe! Suche dir ein Wort aus!").queue();
        if(lastWord != null)getChannel().sendMessage(round + ": <@" + this.player.get(playerTurn) + "> ist an der Reihe! Das letzte Wort ist \"**" + lastWord + "**\"").queue();

        scheduledFuture = Main.executor.schedule(() -> {
            kickPlayer(this.player.get(playerTurn), getChannel().sendMessage(":x: <@" + player +"> ist Ausgeschieden weil die Zeit abgelaufen ist!"));
            nextTurn();
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

        getChannel().sendMessage(":x: <@" + player + "> hat ein Fehler gemacht und hat nur noch **" + lives + "** Versuche!").queue();
    }

    private void kickPlayer(long player, RestAction<?> restAction) {
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
                .addField("Spieler:", player.stream().map(p -> ("<@" + p + ">")).collect(Collectors.joining("\r\n")), true)
                .setFooter("GameID: " + uuid)
                .setTimestamp(Instant.now())
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(status != GameStatus.PLAYING) return;
        if (event.getChannel() != getChannel()) return;
        long memberID = event.getMember().getIdLong();
        if (memberID != player.get(playerTurn)) return;

        String content = event.getMessage().getContentRaw();

        if(lastWord != null) {
            if (content.split(" ").length > 1 ||
                    Character.toLowerCase(content.toCharArray()[0]) != Character.toLowerCase(lastWord.charAt(lastWord.length()-1)) ||
                    words.contains(content.split(" ")[0].toLowerCase())) {
                damage(memberID);
            } else {
                lastWord = content.split(" ")[0].toLowerCase();
                words.add(lastWord);
            }
        } else {
            lastWord = content.split(" ")[0].toLowerCase();
            words.add(lastWord);
        }
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

                getChannel().sendMessage("<@" + event.getMember().getId() + "> ist dem Spiel beigetreten!").queue();
                return;
            }
            case "leave" -> {
                if (!player.contains(event.getMember().getIdLong())) {
                    event.reply(":x: Du bist nicht im Game!").setEphemeral(true).queue();
                    return;
                }
                if (event.getMember().getIdLong() == gameMaster) {
                    event.reply(":x: Du bist Spielleiter:in du kannst nicht verlassen!").setEphemeral(true).queue();
                    return;
                }

                leave(event.getMember().getIdLong());

                event.editMessageEmbeds(updateJoinEmbed(event.getMessage().getEmbeds().get(0))).queue();

                event.reply(":x: <@" + event.getMember().getId() + "> ist dem Spiel verlassen!").queue();

            }
        }

        if(event.getMember().getIdLong() != gameMaster) {
            event.reply(":x: Du bist nicht Spielleiter:in!").queue();
            return;
        }
        switch(id) {
            case "start" -> {
                if (player.size() < 2) {
                    event.reply(":x: Es müssen mindestens 2 Spieler:innen mit machen!").setEphemeral(true).queue();
                    return;
                }
                event.getMessage().delete().queue();
                start();
            }
        }
    }
}
