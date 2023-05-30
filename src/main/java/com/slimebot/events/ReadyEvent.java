package com.slimebot.events;


import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;

public class ReadyEvent extends ListenerAdapter {

    @Override
    public void onReady(@NotNull net.dv8tion.jda.api.events.session.ReadyEvent event) {
        super.onReady(event);

        for (Guild guild : Main.getJDAInstance().getGuilds()) {
            try {
                Config.createFileWithDir("config", guild.getId(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        Class<? extends TextChannel> TextChannel;
        TextChannel DEVlogChannel = event.getJDA().getTextChannelById("1080912327693574275");//Channel only for dev not for each Discord

        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("Bot wurde gestartet")
                .setDescription("Der Bot hat sich mit der DiscordAPI (neu-) verbunden")
                .setColor(Main.embedColor(DEVlogChannel.getGuild().getId()))
                .setTimestamp(new Date().toInstant());

        MessageEmbed em = embed.build();

        DEVlogChannel.sendMessageEmbeds(em).queue();


    }


}
