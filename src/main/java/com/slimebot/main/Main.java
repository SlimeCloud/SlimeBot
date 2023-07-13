package com.slimebot.main;

import com.slimebot.alerts.holidays.HolidayAlert;
import com.slimebot.alerts.spotify.SpotifyListenerManager;
import com.slimebot.commands.*;
import com.slimebot.commands.report.MessageReportCommand;
import com.slimebot.commands.report.ReportCommand;
import com.slimebot.commands.report.UserReportCommand;
import com.slimebot.events.JoinListener;
import com.slimebot.events.StartupListener;
import com.slimebot.events.TimeoutListener;
import com.slimebot.message.StaffMessage;
import com.slimebot.utils.Config;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.ContextBase;
import de.mineking.discord.commands.ContextCreator;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
	public final static Logger logger = LoggerFactory.getLogger(Main.class);

	public final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);

	private static final String activityText = Config.getBotInfo("activity.text");
	public static String activityType = Config.getBotInfo("activity.type");

	public static JDA jdaInstance;
	public static DiscordUtils discordUtils;

	public final static SpotifyListenerManager spotify = new SpotifyListenerManager();

	public static List<String> blocklist(String guildID) {
		YamlFile config = Config.getConfig(guildID, "mainConfig");
		try {
			config.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return config.getStringList("blocklist");
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
		logger.info("Bot Version: {}", Config.getBotInfo("version"));
		if(args.length == 0) {
			logger.error("BITTE EIN TOKEN NAME ALS COMMAND-LINE-PARAMETER ÜBERGEBEN (.env im bot-ordner)");
			System.exit(420);
		}

		String tokenName = args[0];

		logger.info("{}-Bot wird gestartet...", tokenName);
		String token = Config.getEnvKey("TOKEN_" + tokenName.toUpperCase());
		if(token == null || token.isEmpty()) {
			logger.error("BITTE EIN TOKEN ANGEBEN (.env im bot-ordner)");
			System.exit(421);
		}

		JDABuilder jdaBuilder = JDABuilder.createDefault(token)
				.setActivity(Activity.of(Activity.ActivityType.valueOf(activityType), activityText))

				.enableIntents(EnumSet.allOf(GatewayIntent.class))
				.setEventPassthrough(true)
				.setMemberCachePolicy(MemberCachePolicy.ALL)

				//Events
				.addEventListeners(new StartupListener())
				.addEventListeners(new TimeoutListener())
				.addEventListeners(new JoinListener())
				.addEventListeners(new StaffMessage());

		discordUtils = new DiscordUtils("", jdaBuilder)
				.useEventManager(null)
				.useUIManager(null)
				.useCommandManager(
						new ContextCreator<>(ContextBase.class, CommandContext::new),
						config -> {
							config.registerCommand(BugCommand.class);
							config.registerCommand(ConfigCommand.class);
							config.registerCommand(BulkAddRoleCommand.class);
							config.registerCommand(PingCommand.class);
							config.registerCommand(FdmdsCommand.class);
							config.registerCommand(InfoCommand.class);

							config.registerCommand(UserReportCommand.class);
							config.registerCommand(MessageReportCommand.class);
							config.registerCommand(ReportCommand.class);
						}
				)
				.useCommandCache(null);

		jdaInstance = discordUtils.build();

		new HolidayAlert(new URL("https://ferien-api.de/api/v1/holidays"));
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
