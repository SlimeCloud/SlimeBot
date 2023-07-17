package com.slimebot.main;

import com.google.gson.Gson;
import com.slimebot.alerts.holidays.HolidayAlert;
import com.slimebot.alerts.spotify.SpotifyListenerManager;
import com.slimebot.commands.*;
import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.commands.report.MessageReportCommand;
import com.slimebot.commands.report.ReportCommand;
import com.slimebot.commands.report.UserReportCommand;
import com.slimebot.events.ReadyListener;
import com.slimebot.events.TimeoutListener;
import com.slimebot.main.config.Config;
import com.slimebot.message.StaffMessage;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.ContextBase;
import de.mineking.discord.commands.ContextCreator;
import de.mineking.discord.commands.inherited.Option;
import de.mineking.discord.list.ListCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
	public final static String version = "2.0.0"; //TODO Automatically replace this from gradle build

	public final static Logger logger = LoggerFactory.getLogger(Main.class);
	public final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);
	public final static Gson gson = new Gson();

	static {
		ListCommand.pageOption = new Option(OptionType.INTEGER, "seite", "Startseite").range(1, null);
	}

	public static Config config;

	public static JDA jdaInstance;

	public static Database database;
	public static DiscordUtils discordUtils;

	public static SpotifyListenerManager spotify;
	public static HolidayAlert holiday;

	public static void main(String[] args) throws IOException {
		config = Config.readFromFile("config");

		logger.info("Bot Version: {}", version);
		if(args.length == 0) {
			logger.error("BITTE EIN TOKEN NAME ALS COMMAND-LINE-PARAMETER ÜBERGEBEN (.env im bot-ordner)");
			System.exit(420);
		}

		String tokenName = args[0];

		logger.info("{}-Bot wird gestartet...", tokenName);
		String token = Config.env.get("TOKEN_" + tokenName.toUpperCase());
		if(token == null || token.isEmpty()) {
			logger.error("BITTE EIN TOKEN ANGEBEN (.env im bot-ordner)");
			System.exit(421);
		}

		database = new Database();

		JDABuilder jdaBuilder = JDABuilder.createDefault(token)
				.setActivity(config.activity.build())

				.enableIntents(EnumSet.allOf(GatewayIntent.class))
				.setEventPassthrough(true)
				.setMemberCachePolicy(MemberCachePolicy.ALL)

				//Events
				.addEventListeners(new ReadyListener())
				.addEventListeners(new TimeoutListener())
				.addEventListeners(new StaffMessage());

		discordUtils = new DiscordUtils("", jdaBuilder)
				.useCustomRestactionManager(null)
				.useEventManager(null)
				.useListCommands(null)
				.useCommandManager(
						new ContextCreator<>(ContextBase.class, CommandContext::new),
						config -> {
							config.registerCommand(ConfigCommand.class);

							config.registerCommand(BugCommand.class);
							config.registerCommand(BulkAddRoleCommand.class);
							config.registerCommand(PingCommand.class);
							config.registerCommand(FdmdsCommand.class);
							config.registerCommand(InfoCommand.class);
							config.registerCommand(BonkCommand.class);
							config.registerCommand(ContributorCommand.class);

							config.registerCommand(UserReportCommand.class);
							config.registerCommand(MessageReportCommand.class);
							config.registerCommand(ReportCommand.class);
						}
				)
				.useCommandCache(null);

		jdaInstance = discordUtils.build();

		if(config.spotify != null) {
			spotify = new SpotifyListenerManager();
		}

		else {
			logger.info("No spotify configuration found - Disabled spotify notifications");
		}

		holiday = new HolidayAlert();
	}

	public static void updateGuildCommands(Guild guild) {
		discordUtils.getCommandCache().updateGuildCommands(guild,
				Map.of("fdmds", database.handle(handle -> handle.createQuery("select count(*) from fdmds where guild = :guild")
						.bind("guild", guild.getIdLong())
						.mapTo(int.class)
						.one()
				) > 0),
				error -> logger.error("Failed to update guild commands for " + guild, error)
		);
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
