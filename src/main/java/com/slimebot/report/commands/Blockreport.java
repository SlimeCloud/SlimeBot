package com.slimebot.report.commands;

import com.slimebot.utils.Checks;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

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
            event.reply("kein Teammitglied!").queue();
            return;
        }


        switch (event.getOption("action").getAsString()) {
            case "add" -> {
                if (Main.blocklist.contains(event.getOption("user").getAsMember())) {
                    event.reply("Ist bereits geblocked").setEphemeral(true).queue();
                    return;
                }
                Main.blocklist.add(event.getOption("user").getAsMember());
                event.reply(event.getOption("user").getAsMentionable().getAsMention() + " wurde geblockt und kann nun keine Reports mehr erstellen").queue();
            }
            case "remove" -> {
                if (!(Main.blocklist.contains(event.getOption("user").getAsMember()))) {
                    event.reply("Ist nicht geblocked").setEphemeral(true).queue();
                    return;
                }
                Main.blocklist.remove(event.getOption("user").getAsMember());
                event.reply(event.getOption("user").getAsMentionable().getAsMention() + " kann nun wieder Reports erstellen").queue();
            }
            case "list" -> {
                StringBuilder msg = new StringBuilder();
                for (Member member : Main.blocklist) {
                    msg.append(member.getAsMention()).append("\n");
                }
                event.reply("Blocked: \n" + msg).queue();
            }
            default -> event.reply("Etwas ist schief gelaufen bitte kontaktiere einen Netrunner").queue();
        }


    }
}
