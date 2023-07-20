package com.slimebot.games.games;

import com.slimebot.games.Game;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Wordchain extends Game {
    private Character character;
    private int playerTurn;
    private int round;
    private ScheduledFuture<?> scheduledFuture;
    private short seconds = 15;
    private short lives = 3;
    private Map<Long, Short> playerLives;

    public Wordchain(long gameMaster, MessageChannel channel) {
        super(gameMaster, channel);
        this.playerLives = new HashMap<>();
    }



    @Override
    public void start() {
        super.start();

        player.forEach(p -> playerLives.put(p, lives));

        round = 0;
        playerTurn = 0;

        nextTurn();
    }

    @Override
    public void create() {
        super.create();

        // TODO: Joining Embed Stuff, and Start Button for the Game master
    }

    private void nextTurn() {
        if(scheduledFuture != null)scheduledFuture.cancel(true);

        if(player.size() == 1) {
            // TODO: Winn stuff
            end();
        }

        round++;
        if((playerTurn+1) < this.player.size()) {
            playerTurn++;
        }else {
            playerTurn = 0;
        }

        scheduledFuture = Main.executor.schedule(() -> {
            kickPlayer(this.player.get(playerTurn), channel.sendMessage("")); // TODO: message
            nextTurn();
        }, seconds, TimeUnit.SECONDS);
    }

    private void damage(long player) {
        short lives = playerLives.get(player);
        if(lives <= 1) {
            kickPlayer(player, channel.sendMessage("")); // TODO: player scheidet aus message
            playerLives.remove(player);
            return;
        }
        lives--;
        playerLives.put(player, lives);
    }

    private void kickPlayer(long player, RestAction<?> restAction) {
        restAction.queue();

        leave(player);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getChannel() != channel)return;
        long memberID = event.getMember().getIdLong();
        if(memberID != player.get(playerTurn))return;

        String content = event.getMessage().getContentRaw();

        if(content.split(" ").length > 1 || content.toCharArray()[0] != character)damage(memberID);

        character = content.toCharArray()[content.length()-1];
        nextTurn();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getId().startsWith("wordchain"))return;

        // TODO: JOIN Button

        if(event.getMember().getIdLong() != gameMaster) {
            event.reply("Du bist nicht der Game Master! uiashdfuihwesuiafgh").queue(); // TODO: better message
            return;
        }
        switch(event.getId()) {
            case "wordchain:settings:create":
                create();
                event.getMessage().delete().queue();
                return;
            case "wordchain:start":
                if(player.size() >= 2)start();
                return;

        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if(!event.getId().startsWith("wordchain"))return;
        if(event.getMember().getIdLong() != gameMaster) {
            event.reply("Du bist nicht der Game Master! uiashdfuihwesuiafgh").queue(); // TODO: better message
            return;
        }
        switch(event.getId()) {
            case "wordchain:settings:time" :
                this.seconds = Short.valueOf(event.getSelectedOptions().get(0).getValue());
                break;
            case "wordchain:settings:lives" :
                this.lives = Short.valueOf(event.getSelectedOptions().get(0).getValue());
                break;
        }
    }
}
