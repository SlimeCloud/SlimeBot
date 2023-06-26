package com.slimebot.main;

import com.slimebot.commands.*;
import com.slimebot.events.OnJoin;
import com.slimebot.events.ReadyEvent;
import com.slimebot.events.Timeout;
import com.slimebot.report.assets.Report;
import com.slimebot.report.buttons.Close;
import com.slimebot.report.buttons.DetailDropdown;
import com.slimebot.report.commands.Blockreport;
import com.slimebot.report.commands.ReportCmd;
import com.slimebot.report.commands.GetReportDetail;
import com.slimebot.report.commands.ReportList;
import com.slimebot.report.contextmenus.MsgReport;
import com.slimebot.report.contextmenus.UserReport;
import com.slimebot.report.modals.CloseReport;
import com.slimebot.report.modals.ReportModal;
import com.slimebot.utils.Config;
import com.slimebot.utils.TimeScheduler;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.simpleyaml.configuration.file.YamlFile;

import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    public static JDA jdaInstance;
    private static final String activityText = Config.getBotInfo("activity.text");
    public static String activityType = Config.getBotInfo("activity.type");
    public static ArrayList<String> blocklist(String guildID) {
        YamlFile config = Config.getConfig(guildID, "mainConfig");
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return (ArrayList<String>) config.getStringList("blocklist");
    }
    public static Color embedColor(String guildID){
        YamlFile config = Config.getConfig(guildID, "mainConfig");
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Color(
                config.getInt("embedColor.red"),
                config.getInt("embedColor.green"),
                config.getInt("embedColor.blue")
            );
    }
    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss ");

    public static void main(String[] args) throws IOException {
        System.out.println("Bot Version: "+ Config.getBotInfo("version"));

        System.out.println("Welcher Bot soll gestartet werden? 'main' oder 'test'");
        Scanner in = new Scanner(System.in);
        String inToken = in.nextLine();
        String token = Config.getBotInfo("token."+inToken.toLowerCase());
        if (Objects.equals(token, "")){missingToken();}

        jdaInstance = JDABuilder.createDefault(token)
                .setActivity(Activity.of(getActivityType(activityType), activityText))

                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .setEventPassthrough(true)
                .setMemberCachePolicy(MemberCachePolicy.ALL)

                // Commands
                .addEventListeners(new Bug())
                .addEventListeners(new ConfigCmd())
                .addEventListeners(new BulkAddRole())
                .addEventListeners(new Ping())
                .addEventListeners(new Blockreport())
                .addEventListeners(new ReportCmd())
                .addEventListeners(new GetReportDetail())
                .addEventListeners(new ReportList())
                .addEventListeners(new Info())

                //Events
                .addEventListeners(new ReadyEvent())
                .addEventListeners(new Timeout())
                .addEventListeners(new OnJoin())

                //Context Menus
                .addEventListeners(new MsgReport())
                .addEventListeners(new UserReport())

                //Modals
                .addEventListeners(new ReportModal())
                .addEventListeners(new CloseReport())

                //Buttons
                .addEventListeners(new Close())
                .addEventListeners(new DetailDropdown())

                .build();

        //Register Commands
        jdaInstance.upsertCommand(Commands.slash("bug", "Melde einen Bug")).queue();

        jdaInstance.upsertCommand(Commands.slash("config", "Nehme Änderungen an der Konfiguration vor")
                .addOptions(new OptionData(OptionType.STRING, "type", "Welcher Config-Bereich?")
                        .setRequired(true)
                        .addChoice("Allgemeine Konfiguration", "config"))
                .addOptions(new OptionData(OptionType.STRING, "field", "Welches Feld soll angepasst werden?")
                        .addChoice("Log Channel (ID)","logChannel")
                        .addChoice("Blockliste","blocklist")
                        .addChoice("Team Rolle (ID)","staffRoleID")
                        .addChoice("Verification Rolle (ID)","verifyRoleID")
                        .addChoice("Warning Channel (ID)","punishmentChannelID")
                        .addChoice("Embed Color (RGB) Rot","embedColor.rgb.red")
                        .addChoice("Embed Color (RGB) Grün","embedColor.rgb.green")
                        .addChoice("Embed Color (RGB) Blau","embedColor.rgb.blue")
                        .setRequired(true))
                .addOptions(new OptionData(OptionType.STRING, "value", "Welcher Wert soll bei dem Feld gesetzt werden?")
                        .setRequired(true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
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

        jdaInstance.upsertCommand(Commands.slash("report_list", "Lasse dir Reports sortiert nach ihrem Status anzeigen")
                .addOptions(new OptionData(OptionType.STRING, "status", "Setze einen Filter für die Reports")
                        .setRequired(true)
                        .addChoice("Alle", "all")
                        .addChoice("Geschlossen", "closed")
                        .addChoice("Offen", "open")
                )
        ).queue();

        jdaInstance.upsertCommand(Commands.slash("report_detail", "Lasse dir die Details zu einem Report anzeigen")
                .addOption(OptionType.INTEGER, "id", "ID des Reports den du genauer ansehen willst", true)
        ).queue();

        jdaInstance.upsertCommand(Commands.slash("info", "Bekomme genauere Informationen über den Bot")).queue();


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
                for (Guild guild : getJDAInstance().getGuilds()) {
                    Config.createMain(guild.getId());

                }
            }
        });
    }

    public static void missingToken(){
        YamlFile botConfig = new YamlFile("Slimebot/main/botConfig.yml");
        System.out.println("\n\nBITTE TRAGEN DEN TOKEN EIN\n"+botConfig.getFilePath()+"\n\n");
        System.exit(800);
    }

    public static JDA getJDAInstance() {
        return jdaInstance;
    }

    private static Activity.ActivityType getActivityType(String aType) {

        return switch (aType) {
            case "WATCHING" -> Activity.ActivityType.WATCHING;
            case "STREAMING" -> Activity.ActivityType.STREAMING;
            case "LISTENING" -> Activity.ActivityType.LISTENING;
            case "PLAYING" -> Activity.ActivityType.PLAYING;
            case "COMPETING" -> Activity.ActivityType.COMPETING;
            default -> Activity.ActivityType.CUSTOM_STATUS;
        };
    }
}
