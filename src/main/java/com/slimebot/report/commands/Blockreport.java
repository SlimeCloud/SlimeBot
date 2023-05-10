package com.slimebot.report.commands;

import com.slimebot.utils.Checks;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;

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
                    .setColor(Main.embedColor)
                    .setTitle(":exclamation: Error")
                    .setDescription("Der Befehl kann nur von einem Teammitglied ausgeführt werden!");
            event.replyEmbeds(noTeam.build()).queue();
            return;
        }


        switch (event.getOption("action").getAsString()) {
            case "add" -> {
                if (Main.blocklist.contains(event.getOption("user").getAsMember())) {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                            .setColor(Main.embedColor)
                            .setTitle(":exclamation: Error: Already blocked!")
                            .setDescription(event.getOption("user").getAsMember().getAsMention() + " ist bereits blockiert");
                    event.replyEmbeds(embedBuilder.build()).queue();
                    return;
                }
                Main.blocklist.add(event.getOption("user").getAsMember());
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                        .setColor(Main.embedColor)
                        .setTitle(":white_check_mark: Erfolgreich Blockiert")
                        .setDescription(event.getOption("user").getAsMentionable().getAsMention() + " wurde blockiert und kann nun keine Reports mehr erstellen");
                event.replyEmbeds(embedBuilder.build()).queue();
            }
            case "remove" -> {
                if (!(Main.blocklist.contains(event.getOption("user").getAsMember()))) {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                            .setColor(Main.embedColor)
                            .setTitle(":exclamation: Error: Not Found")
                            .setDescription(event.getOption("user").getAsMember() + " konnte nicht in der Blockliste gefunden werden!");
                    event.replyEmbeds(embedBuilder.build()).queue();
                    return;
                }
                Main.blocklist.remove(event.getOption("user").getAsMember());
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                        .setColor(Main.embedColor)
                        .setTitle(":white_check_mark: Entblockt")
                        .setDescription(event.getOption("user").getAsMentionable().getAsMention() + " kann nun wieder Reports erstellen");
                event.replyEmbeds(embedBuilder.build()).queue();
            }
            case "list" -> {
                StringBuilder msg = new StringBuilder();
                for (Member member : Main.blocklist) {
                    msg.append(member.getAsMention()).append("\n");
                }
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                        .setColor(Main.embedColor)
                        .setTitle("Geblockte User:")
                        .setDescription("Folgende Member sind blockiert und können keine Reports mehr erstellen:\n" + msg);
                event.replyEmbeds(embedBuilder.build()).queue();
            }
            default -> event.reply(":exclamation: **Error!** \nEtwas ist schief gelaufen bitte kontaktiere einen Netrunner\nDev Info: com.slimebot.report.commands.Blockreport Zeile 85").queue();
        }


    }
}
