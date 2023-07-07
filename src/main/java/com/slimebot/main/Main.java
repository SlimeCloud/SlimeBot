package com.slimebot.main;

import com.slimebot.alerts.spotify.SpotifyListenerManager;
import com.slimebot.commands.*;
import com.slimebot.events.OnJoin;
import com.slimebot.events.ReadyEvent;
import com.slimebot.events.Timeout;
import com.slimebot.report.buttons.Close;
import com.slimebot.report.buttons.DetailDropdown;
import com.slimebot.report.commands.Blockreport;
import com.slimebot.report.commands.GetReportDetail;
import com.slimebot.report.commands.ReportCmd;
import com.slimebot.report.commands.ReportList;
import com.slimebot.report.contextmenus.MsgReport;
import com.slimebot.report.contextmenus.UserReport;
import com.slimebot.report.modals.CloseReport;
import com.slimebot.report.modals.ReportModal;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.simpleyaml.configuration.file.YamlFile;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
	public final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);

	private static final String activityText = Config.getBotInfo("activity.text");
	public static String activityType = Config.getBotInfo("activity.type");

	public static JDA jdaInstance;
	public static SpotifyListenerManager spotify;

	public static ArrayList<String> blocklist(String guildID) {
		YamlFile config = Config.getConfig(guildID, "mainConfig");
		try {
			config.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return (ArrayList<String>) config.getStringList("blocklist");
	}

	public static Color embedColor(String guildID) {
		YamlFile config = Config.getConfig(guildID, "mainConfig");
		try {
			config.load();
		} catch(IOException e) {
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
		System.out.println("Bot Version: " + Config.getBotInfo("version"));
		if(args.length == 0) {
			System.out.println("\n\nPLEASE PROVIDE A TOKEN NAME AS PARAMETER\n\n");
			System.exit(420);
		}

		String tokenName = args[0];

		System.out.println(tokenName + "-Bot wird gestartet...");
		String token = Config.getEnvKey("TOKEN_" + tokenName.toUpperCase());
		if(token == null || token.isEmpty()) {
			System.out.println("\n\nPLEASE ADD A TOKEN (.env in root dir)\n\n");
			System.exit(421);
		}

		jdaInstance = JDABuilder.createDefault(token)
				.setActivity(Activity.of(Activity.ActivityType.valueOf(activityType), activityText))

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
				.addEventListeners(new Fdmds())

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

		registerCommands();

		//Register Spotify Hooks
		spotify = new SpotifyListenerManager();
	}

	public static void registerCommands() {
		jdaInstance.updateCommands().addCommands(
				Commands.slash("bug", "Melde einen Bug"),

				Commands.slash("config", "Nehme Änderungen an der Konfiguration vor")
						.addOptions(new OptionData(OptionType.STRING, "type", "Welcher Config-Bereich?")
								.setRequired(true)
								.addChoice("Allgemeine Konfiguration", "config"))
						.addOptions(new OptionData(OptionType.STRING, "field", "Welches Feld soll angepasst werden?")
								.addChoice("Log Channel (ID)", "logChannel")
								.addChoice("Blockliste", "blocklist")
								.addChoice("Team Rolle (ID)", "staffRoleID")
								.addChoice("Verification Rolle (ID)", "verifyRoleID")
								.addChoice("Warning Channel (ID)", "punishmentChannelID")
								.addChoice("Embed Color (RGB) Rot", "embedColor.rgb.red")
								.addChoice("Embed Color (RGB) Grün", "embedColor.rgb.green")
								.addChoice("Embed Color (RGB) Blau", "embedColor.rgb.blue")
								.setRequired(true))
						.addOptions(new OptionData(OptionType.STRING, "value", "Welcher Wert soll bei dem Feld gesetzt werden?")
								.setRequired(true))
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

				Commands.slash("ping", "ping pong"),

				Commands.slash("role_check", "[Team Befehl] Geht ALLE Mitglieder durch und gibt ihnen eine Rolle")
						.addOption(OptionType.ROLE, "rolle", "Auf welche Rolle sollen die User überprüft werden?", true)
						.addOption(OptionType.BOOLEAN, "bots", "Sollen Bots mit überprüft werden?", true),
				Commands.slash("blockreport", "Blocke eine Person das sie keine Reports mehr erstellen kann")
						.addOptions(new OptionData(OptionType.USER, "user", "Wähle einen User aus")
								.setRequired(true))
						.addOptions(new OptionData(OptionType.STRING, "action", "Wähle aus was du machen möchtest")
								.setRequired(true)
								.addChoice("add", "add")
								.addChoice("remove", "remove")
								.addChoice("list", "list")
						),

				Commands.slash("report", "Reporte eine Person")
						.addOption(OptionType.USER, "user", "Wähle aus wen du melden möchtest", true)
						.addOption(OptionType.STRING, "beschreibung", "Warum möchtest du den User reporten?", true),
				Commands.slash("report_list", "Lasse dir Reports sortiert nach ihrem Status anzeigen")
						.addOptions(new OptionData(OptionType.STRING, "status", "Setze einen Filter für die Reports")
								.setRequired(true)
								.addChoice("Alle", "all")
								.addChoice("Geschlossen", "closed")
								.addChoice("Offen", "open")
						),
				Commands.slash("report_detail", "Lasse dir die Details zu einem Report anzeigen")
						.addOption(OptionType.INTEGER, "id", "ID des Reports den du genauer ansehen willst", true),

				Commands.slash("info", "Bekomme genauere Informationen über den Bot"),
				Commands.slash("fdmds", "Schlage eine Frage für \"Frag doch mal den Schleim\" vor!"),

				Commands.context(Command.Type.USER, "Report User"),
				Commands.context(Command.Type.MESSAGE, "Report Message")
		).queue();
	}

	public static void scheduleDaily(int hour, Runnable task) {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime nextRun = now.withHour(hour).withMinute(0).withSecond(0);
		if(now.compareTo(nextRun) > 0)
			nextRun = nextRun.plusDays(1);

		long initialDelay = Duration.between(now, nextRun).getSeconds();

		executor.scheduleAtFixedRate(task, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
	}
}
