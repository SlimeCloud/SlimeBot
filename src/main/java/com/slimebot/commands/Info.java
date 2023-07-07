package com.slimebot.commands;

import com.slimebot.utils.Config;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Info extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
        if (!(event.getName().equals("info"))) {
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Informationen über den SlimeBall bot")
                .setColor(Main.embedColor(event.getGuild().getId()))
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setDescription("Dieser Bot ist ein Custom bot des SlimeCloud Discords und stellt Features bereit die so von keinem anderen Bot gelöst werden können.")
                .addField("Gecodet von:", "[SlimeCloud DevTeam](https://github.com/SlimeCloud)", true)
                .addField("Version:", Config.getBotInfo("version"), true)//ToDo get Version form build.gradle
                .addField("Support:", "Bei Fragen, Verbesserungen, Bugs öffne ein Ticket", true)
                .addField("Prefix:", "Dieser Bot nutzt Slash Commands", true)
                .setFooter("SlimeBall", "https://media.discordapp.net/attachments/1098639892608712714/1098639949592539166/SlimeBall.png");

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
