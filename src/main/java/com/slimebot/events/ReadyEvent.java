package com.slimebot.events;


import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class ReadyEvent extends ListenerAdapter {

    @Override
    public void onReady(@NotNull net.dv8tion.jda.api.events.session.ReadyEvent event) {
        super.onReady(event);


        for (Guild guild : Main.getJDAInstance().getGuilds()) {
            YamlFile config = Config.getConfig(guild.getId(), "mainConfig");
            YamlFile reportFile = Config.getConfig(guild.getId(), "reports");

            if (!(config.exists())){
                Config.createMain(guild.getId());
            }

            if (!(reportFile.exists())){
                try {
                    reportFile.createNewFile();
                    reportFile.load();
                    reportFile.set("reports.abc","def");
                    reportFile.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }



            }
        }



        TextChannel DEVlogChannel = event.getJDA().getTextChannelById("978351336485683214");//Channel only for dev not for each Discord

        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("Bot wurde gestartet")
                .setDescription("Der Bot hat sich mit der DiscordAPI (neu-) verbunden")
                .setColor(Main.embedColor(DEVlogChannel.getGuild().getId()))
                .setTimestamp(new Date().toInstant());

        MessageEmbed em = embed.build();

        DEVlogChannel.sendMessageEmbeds(em).queue();


    }


}
