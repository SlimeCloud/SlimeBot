package com.slimebot.events;


import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class ReadyEvent extends ListenerAdapter {

    @Override
    public void onReady(@NotNull net.dv8tion.jda.api.events.session.ReadyEvent event) {
        super.onReady(event);

        Class<? extends TextChannel> TextChannel;
        TextChannel logChannel = event.getJDA().getTextChannelById("1080912327693574275");


        EmbedBuilder embed = new EmbedBuilder();



        embed.setTitle("Bot wurde gestartet")
                .setDescription("Der Bot hat sich mit der DiscordAPI (neu-) verbunden")
                .setColor(Main.embedColor)
                .setTimestamp(new Date().toInstant());

        MessageEmbed em = embed.build();

        assert logChannel != null;
        logChannel.sendMessageEmbeds(em).queue();


    }
}
