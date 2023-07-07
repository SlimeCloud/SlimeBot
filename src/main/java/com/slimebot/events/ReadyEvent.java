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

            try {
                config.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (!(config.exists())){
                Config.createMain(guild.getId());
            } else {
                TextChannel channel = Main.getJDAInstance().getGuildById(guild.getId()).getTextChannelById(config.getString("logChannel"));

                if (channel == null)return;

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle("Bot wurde gestartet")
                        .setDescription("Der Bot hat sich mit der DiscordAPI (neu-) verbunden")
                        .setColor(Main.embedColor(guild.getId()))
                        .setTimestamp(new Date().toInstant());

                MessageEmbed em = embed.build();


                channel.sendMessageEmbeds(em).queue();
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




    }


}
