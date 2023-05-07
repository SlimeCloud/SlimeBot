package com.slimebot.main;

import com.slimebot.commands.Bug;
import com.slimebot.commands.BulkAddRole;
import com.slimebot.commands.Ping;
import com.slimebot.events.ReadyEvent;
import com.slimebot.report.commands.Blockreport;
import com.slimebot.report.commands.ReportCmd;
import com.slimebot.report.commands.GetReportDetail;
import com.slimebot.report.contextmenus.MsgReport;
import com.slimebot.report.contextmenus.UserReport;
import com.slimebot.report.modals.ReportModal;
import com.slimebot.utils.Config;
import com.slimebot.utils.TimeScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;

public class Main {
    public static JDA jdaInstance;
    private static String activityText = Config.getLocalProperty("config.properties", "main.activity.text");
    private static String activityType = Config.getLocalProperty("config.properties", "main.activity");
    public static ArrayList<Member> blocklist = new ArrayList<>(); //todo get From Config or DataBase
    public static ArrayList<com.slimebot.report.assets.Report> reports = new ArrayList<>(); //ToDo get From Config or DataBase
    public static Color embedColor = new Color(86,157,60); //ToDo get From Config or DataBase so you can Change the Color via cmd

    public static void main(String[] args) {
        jdaInstance = JDABuilder.createDefault(Config.getLocalProperty("config.properties", "main.token"))
                .setActivity(Activity.of(getActivityType(activityType), activityText))

                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)

                // Commands
                .addEventListeners(new Bug())
                .addEventListeners(new com.slimebot.commands.Config())
                .addEventListeners(new BulkAddRole())
                .addEventListeners(new Ping())
                .addEventListeners(new Blockreport())
                .addEventListeners(new ReportCmd())
                .addEventListeners(new GetReportDetail())


                //Events
                .addEventListeners(new ReadyEvent())

                //Context Menus
                .addEventListeners(new MsgReport())
                .addEventListeners(new UserReport())


                //Modals
                .addEventListeners(new ReportModal())


                .build();

        //Register Commands
        jdaInstance.upsertCommand(Commands.slash("bug", Config.getLocalProperty("bug.properties", "bug.commandDesc"))).queue();

        jdaInstance.upsertCommand(Commands.slash("config", Config.getLocalProperty("config.properties", "config.commandDesc"))
                .addOptions(new OptionData(OptionType.STRING, "type", "Welcher Config-Bereich?")
                        .setRequired(true)
                        .addChoice("Allgemeine Konfiguration", "config"))
                .addOptions(new OptionData(OptionType.STRING, "field", "Welches Feld soll angepasst werden?")
                        .setRequired(true))
                .addOptions(new OptionData(OptionType.STRING, "value", "Welcher Wert soll bei dem Feld gesetzt werden?")
                        .setRequired(true))
        ).queue();

        jdaInstance.upsertCommand(Commands.slash("ping", "ping pong")).queue();

        jdaInstance.upsertCommand(Commands.slash("role_check", "[Team Befehl] Geht ALLE Mitglieder durch und gibt ihnen eine Rolle")
                .addOption(OptionType.ROLE, "rolle", "Auf welche Rolle sollen die User überprüft werden?", true)
                .addOption(OptionType.BOOLEAN, "bots", "Sollen Bots mit überprüft werden?", true)
        ).queue();

        jdaInstance.upsertCommand(Commands.slash("blockreport", "Blocke eine Person das sie keine Reports mehr erstellen kann")
                .addOptions(new OptionData(OptionType.USER, "user", "Wähle einen User aus")
                        .setRequired(true))
                .addOptions(new OptionData(OptionType.STRING, "action", "Wähle aus was du machen möchtest")
                        .setRequired(true)
                        .addChoice("add", "add")
                        .addChoice("remove", "remove")
                        .addChoice("list", "list")
                )
        ).queue();

        jdaInstance.upsertCommand(Commands.slash("report", "Reporte eine Person")
                .addOption(OptionType.USER, "user", "Wähle aus wen du melden möchtest", true)
                .addOption(OptionType.STRING, "beschreibung", "Warum möchtest du den User reporten?", true)
        ).queue();

        jdaInstance.upsertCommand(Commands.slash("getReport", "Lasse dir die Details zu einem Report anzeigen")
                .addOptions(new OptionData(OptionType.STRING, "status", "Setze einen Filter für die Reports")
                        .setRequired(true)
                        .addChoice("Alle", "all")
                        .addChoice("Geschlossen", "closed")
                        .addChoice("Offen", "open")

                )

        ).queue();


        //Register Context Menus
        jdaInstance.upsertCommand(Commands.context(Command.Type.USER, "Report User")).queue();
        jdaInstance.upsertCommand(Commands.context(Command.Type.MESSAGE, "Report Message")).queue();

        jdaInstance.updateCommands();

        checkForGuilds();
    }


    public static void checkForGuilds() {
        new TimeScheduler(300).startTimer(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Check for new Guilds");
                for (Guild guild : getJDAInstance().getGuilds()) {
                    try {
                        Config.createFileWithDir(Config.botPath + guild.getId(), "/config.yml", true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public static JDA getJDAInstance() {
        return jdaInstance;
    }

    private static Activity.ActivityType getActivityType(String type) {
        Activity.ActivityType activityType;

        switch (type) {
            case "WATCHING":
                activityType = Activity.ActivityType.WATCHING;
                break;
            case "STREAMING":
                activityType = Activity.ActivityType.STREAMING;
                break;
            case "LISTENING":
                activityType = Activity.ActivityType.LISTENING;
                break;
            case "PLAYING":
                activityType = Activity.ActivityType.PLAYING;
                break;
            case "COMPETING":
                activityType = Activity.ActivityType.COMPETING;
                break;
            default:
                activityType = Activity.ActivityType.CUSTOM_STATUS;
        }
        return activityType;
    }

}
