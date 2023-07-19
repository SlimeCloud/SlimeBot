package com.slimebot.games.games;

import com.slimebot.games.Game;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Wordchain extends Game {
    private Character character;
    private int playerTurn;
    private int round;
    private ScheduledFuture<?> scheduledFuture;

    public Wordchain(long gameMaster, MessageChannel channel) {
        super(gameMaster, channel);

        // TODO: Joining Embed Stuff, and Start Button for the Game master
    }

    @Override
    public void start() {
        super.start();

        round = 0;
        playerTurn = 0;

        nextTurn();
    }

    private void nextTurn() {
        scheduledFuture.cancel(true);

        if(player.size() == 1) {
            // TODO: Winn stuff
            end();
        }

        round++;
        if((playerTurn+1) >= this.player.size()) {
            playerTurn++;
        }else {
            playerTurn = 0;
        }

        scheduledFuture = Main.executor.schedule(() -> {
            kickPlayer(this.player.get(playerTurn), channel.sendMessage("")); // TODO: message
        }, 10, TimeUnit.SECONDS);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getChannel() != channel)return;
        long memberID = event.getMember().getIdLong();
        if(memberID != player.get(playerTurn))return;
        String content = event.getMessage().getContentRaw();
        if(content.split(" ").length > 1)kickPlayer(memberID, event.getMessage().reply("")); // TODO: message
        if(content.toCharArray()[0] != character)kickPlayer(memberID, event.getMessage().reply("")); // TODO: message
        character = content.toCharArray()[content.length()-1];
        nextTurn();
    }

    private void kickPlayer(long player, RestAction<?> restAction) {
        restAction.queue();

        leave(player);
        nextTurn();
    }
}
