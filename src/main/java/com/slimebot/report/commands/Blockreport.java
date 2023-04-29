package com.slimebot.report.commands;

import com.slimebot.utils.Checks;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;

public class Blockreport extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (!(event.getName().equals("blockreport"))) {
            return;
        }

        if (!Checks.hasTeamRole(event.getMember(), event.getGuild())) {
            event.reply("kein Teammitglied!").queue();
            return;
        }

        ArrayList<Member> blocklist = new ArrayList<>(); //ToDo get From Config or DataBase


        switch (event.getOption("action").getAsString()) {
            case "add":

                if (blocklist.contains((Member) event.getOption("user"))) {
                    event.reply("Ist bereits geblocked").setEphemeral(true).queue();
                    return;
                }

                blocklist.add((Member) event.getOption("user"));

                event.reply((Member) event.getOption("user") + " wurde geblockt und kann nun keine Reports mehr erstellen").queue();
                break;

            case "remove":
                if (!(blocklist.contains((Member) event.getOption("user")))) {
                    event.reply("Ist nicht geblocked").setEphemeral(true).queue();
                    return;
                }

                blocklist.remove((Member) event.getOption("user"));

                event.reply((Member) event.getOption("user") + " kann nun wieder Reports erstellen").queue();
                break;

            case "list":
                StringBuilder msg = new StringBuilder();

                for (Member member: blocklist){
                    msg.append(member.getAsMention()).append("\n");
                }

                event.reply("Blocked: \n" + msg).queue();

                break;

            default:
                event.reply("Etwas ist schief gelaufen bitte kontaktiere einen Netrunner").queue();
                break;


        }


    }
}
