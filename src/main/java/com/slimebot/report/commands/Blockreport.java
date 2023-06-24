package com.slimebot.report.commands;

import com.slimebot.utils.Checks;
import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Blockreport extends ListenerAdapter {



    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);


        if (!(event.getName().equals("blockreport"))) {
            return;
        }


        if (Checks.hasTeamRole(event.getMember(), event.getGuild())) {
            EmbedBuilder noTeam = new EmbedBuilder()
                    .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                    .setColor(Main.embedColor(event.getGuild().getId()))
                    .setTitle(":exclamation: Error")
                    .setDescription("Der Befehl kann nur von einem Teammitglied ausgeführt werden!");
            event.replyEmbeds(noTeam.build()).setEphemeral(true).queue();
            return;
        }


        switch (event.getOption("action").getAsString()) {
            case "add" -> {
                if (Main.blocklist(event.getGuild().getId()).contains(event.getOption("user").getAsMember().getId())) {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                            .setColor(Main.embedColor(event.getGuild().getId()))
                            .setTitle(":exclamation: Error: Already blocked!")
                            .setDescription(event.getOption("user").getAsMember().getAsMention() + " ist bereits blockiert");
                    event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                    return;
                }
                List<String> updatedList = Main.blocklist(event.getGuild().getId());
                updatedList.add(event.getOption("user").getAsMember().getId());
                YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");

                try {
                    config.load();
                    config.set("blocklist", updatedList);
                    config.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                        .setColor(Main.embedColor(event.getGuild().getId()))
                        .setTitle(":white_check_mark: Erfolgreich Blockiert")
                        .setDescription(event.getOption("user").getAsMentionable().getAsMention() + " wurde blockiert und kann nun keine Reports mehr erstellen");
                event.replyEmbeds(embedBuilder.build()).queue();
            }
            case "remove" -> {
                if (!(Main.blocklist(event.getGuild().getId()).contains(event.getOption("user").getAsMember().getId()))) {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                            .setColor(Main.embedColor(event.getGuild().getId()))
                            .setTitle(":exclamation: Error: Not Found")
                            .setDescription(event.getOption("user").getAsMember() + " konnte nicht in der Blockliste gefunden werden!");
                    event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                    return;
                }
                ArrayList<String> updatedList;
                updatedList = Main.blocklist(event.getGuild().getId());
                updatedList.remove(event.getOption("user").getAsMember().getId());
                YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");

                try {
                    config.load();
                    config.set("blocklist", updatedList);
                    config.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                        .setColor(Main.embedColor(event.getGuild().getId()))
                        .setTitle(":white_check_mark: Entblockt")
                        .setDescription(event.getOption("user").getAsMentionable().getAsMention() + " kann nun wieder Reports erstellen");
                event.replyEmbeds(embedBuilder.build()).queue();
            }
            case "list" -> {
                StringBuilder msg = new StringBuilder();
                for (String memberID : Main.blocklist(event.getGuild().getId())) {
                    Member member = event.getGuild().getMemberById(memberID);
                    if (member == null){continue;}
                    msg.append(member.getAsMention()).append("\n");
                }
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                        .setColor(Main.embedColor(event.getGuild().getId()))
                        .setTitle("Geblockte User:")
                        .setDescription("Folgende Member sind blockiert und können keine Reports mehr erstellen:\n" + msg);
                event.replyEmbeds(embedBuilder.build()).queue();
            }
            default -> event.reply(":exclamation: **Error!** \nEtwas ist schief gelaufen bitte kontaktiere einen Netrunner\nDev Info: com.slimebot.report.commands.Blockreport Zeile 85").queue();
        }


    }
}
