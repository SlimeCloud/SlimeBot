package de.slimecloud.slimeball.main;

import de.cyklon.jevent.EventManager;
import de.cyklon.jevent.JEvent;
import de.cyklon.reflection.entities.OfflinePackage;
import de.mineking.databaseutils.DatabaseManager;
import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.commands.Cache;
import de.slimecloud.slimeball.config.ActivityConfig;
import de.slimecloud.slimeball.config.Config;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.LogForwarding;
import de.slimecloud.slimeball.config.commands.ConfigCommand;
import de.slimecloud.slimeball.features.StoredId;
import de.slimecloud.slimeball.features.alerts.IdMemory;
import de.slimecloud.slimeball.features.alerts.holiday.HolidayAlert;
import de.slimecloud.slimeball.features.alerts.spotify.Spotify;
import de.slimecloud.slimeball.features.alerts.spotify.SpotifyAlert;
import de.slimecloud.slimeball.features.alerts.youtube.Youtube;
import de.slimecloud.slimeball.features.alerts.youtube.YoutubeListener;
import de.slimecloud.slimeball.features.birthday.Birthday;
import de.slimecloud.slimeball.features.birthday.BirthdayAlert;
import de.slimecloud.slimeball.features.birthday.BirthdayListener;
import de.slimecloud.slimeball.features.birthday.BirthdayTable;
import de.slimecloud.slimeball.features.birthday.commands.BirthdayCommand;
import de.slimecloud.slimeball.features.fdmds.FdmdsCommand;
import de.slimecloud.slimeball.features.general.*;
import de.slimecloud.slimeball.features.github.BugCommand;
import de.slimecloud.slimeball.features.github.BugContextCommand;
import de.slimecloud.slimeball.features.github.ContributorCommand;
import de.slimecloud.slimeball.features.github.GitHubAPI;
import de.slimecloud.slimeball.features.highlights.Highlight;
import de.slimecloud.slimeball.features.highlights.HighlightTable;
import de.slimecloud.slimeball.features.highlights.commands.HighlightCommand;
import de.slimecloud.slimeball.features.level.Level;
import de.slimecloud.slimeball.features.level.LevelTable;
import de.slimecloud.slimeball.features.level.LevelUpListener;
import de.slimecloud.slimeball.features.level.card.*;
import de.slimecloud.slimeball.features.level.card.badge.BadgeCommand;
import de.slimecloud.slimeball.features.level.card.badge.CardBadgeData;
import de.slimecloud.slimeball.features.level.card.badge.CardBadgeTable;
import de.slimecloud.slimeball.features.level.commands.LevelCommand;
import de.slimecloud.slimeball.features.moderation.MemberJoinListener;
import de.slimecloud.slimeball.features.moderation.MessageListener;
import de.slimecloud.slimeball.features.moderation.TimeoutListener;
import de.slimecloud.slimeball.features.quote.QuoteCommand;
import de.slimecloud.slimeball.features.quote.QuoteMessageCommand;
import de.slimecloud.slimeball.features.report.Report;
import de.slimecloud.slimeball.features.report.ReportBlock;
import de.slimecloud.slimeball.features.report.ReportBlockTable;
import de.slimecloud.slimeball.features.report.ReportTable;
import de.slimecloud.slimeball.features.report.commands.MessageReportCommand;
import de.slimecloud.slimeball.features.report.commands.ReportCommand;
import de.slimecloud.slimeball.features.report.commands.UserReportCommand;
import de.slimecloud.slimeball.features.report.commands.UserReportSlashCommand;
import de.slimecloud.slimeball.features.staff.StaffMessage;
import de.slimecloud.slimeball.features.staff.TeamMeeting;
import de.slimecloud.slimeball.features.staff.absence.Absence;
import de.slimecloud.slimeball.features.staff.absence.AbsenceCommand;
import de.slimecloud.slimeball.features.staff.absence.AbsenceScheduler;
import de.slimecloud.slimeball.features.staff.absence.AbsenceTable;
import de.slimecloud.slimeball.features.statistic.MemberCount;
import de.slimecloud.slimeball.features.statistic.RoleMemberCount;
import de.slimecloud.slimeball.features.wrapped.DataListener;
import de.slimecloud.slimeball.features.wrapped.WrappedData;
import de.slimecloud.slimeball.features.wrapped.WrappedDataTable;
import de.slimecloud.slimeball.main.extensions.*;
import de.slimecloud.slimeball.util.ColorUtil;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class SlimeBot extends ListenerAdapter {

	public static final OfflinePackage botPackage = OfflinePackage.get("de.slimecloud.slimeball");

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);

	private final Config config;
	private final Dotenv credentials;

	private final JDA jda;
	private final DiscordUtils<SlimeBot> discordUtils;

	private final DatabaseManager database;

	private final ReportTable reports;
	private final ReportBlockTable reportBlocks;

	private final LevelTable level;
	private final CardDataTable profileData;
	private final GuildCardTable cardProfiles;
	private final CardBadgeTable cardBadges;

	private final WrappedDataTable wrappedData;
	private final BirthdayTable birthdays;
	private final IdMemory idMemory;
	private final HighlightTable highlights;

	private final AbsenceTable absences;

	private final GitHubAPI github;
	private final Spotify spotify;
	private final Youtube youtube;

	private final MemberCount memberCount;
	private final RoleMemberCount roleMemberCount;

	public SlimeBot(@NotNull Config config, @NotNull Dotenv credentials) throws IOException {
		this.config = config;
		this.credentials = credentials;

		//Verify token
		String token = credentials.get("DISCORD_TOKEN");
		if (token == null) throw new IllegalArgumentException("No token specified");

		Logger jeventLogger = LoggerFactory.getLogger("JEvent");
		JEvent.getDefaultManager().setDebugLogger(jeventLogger::debug);

		//register bot as ParameterInstance
		JEvent.getDefaultManager().registerParameterInstance(this);

		//Initialize database if configured
		String databaseHost = credentials.get("DATABASE_HOST");
		if (databaseHost != null) {
			database = new DatabaseManager("jdbc:postgresql://" + databaseHost, credentials.get("DATABASE_USER"), credentials.get("DATABASE_PASSWORD"));
			database.getDriver().installPlugin(new PostgresPlugin());

			database.putData("bot", this);
			database.addMapper(new SnowflakeTypeMapper());
			database.addMapper(new ColorTypeMapper());
			database.addMapper(new DateTypeMapper());

			//Initialize tables
			reports = database.getTable(Report.class, () -> new Report(this)).name("reports").table(ReportTable.class).create();
			reportBlocks = database.getTable(ReportBlock.class, ReportBlock::new).name("report_blocks").table(ReportBlockTable.class).create();

			level = database.getTable(Level.class, () -> new Level(this)).name("levels").table(LevelTable.class).create();
			profileData = database.getTable(CardProfileData.class, () -> new CardProfileData(this)).name("card_data").table(CardDataTable.class).create();
			cardProfiles = database.getTable(GuildCardProfile.class, () -> new GuildCardProfile(this)).name("guild_card_profiles").table(GuildCardTable.class).create();
			cardBadges = database.getTable(CardBadgeData.class, () -> new CardBadgeData(this)).name("guild_card_badges").table(CardBadgeTable.class).create();

			wrappedData = database.getTable(WrappedData.class, () -> new WrappedData(this)).name("wrapped_data").table(WrappedDataTable.class).create();
			birthdays = database.getTable(Birthday.class, () -> new Birthday(this)).name("birthdays").table(BirthdayTable.class).create();
			idMemory = database.getTable(StoredId.class, () -> new StoredId("", "")).name("id_memory").table(IdMemory.class).create();
			highlights = database.getTable(Highlight.class, () -> new Highlight(this)).name("highlights").table(HighlightTable.class).create();

			absences = database.getTable(Absence.class, () -> new Absence(this)).name("absences").table(AbsenceTable.class).create();
		} else {
			logger.warn("Database credentials missing! Some features will be disabled!");

			database = null;
			reports = null;
			reportBlocks = null;
			level = null;
			profileData = null;
			cardProfiles = null;
			cardBadges = null;
			wrappedData = null;
			birthdays = null;
			idMemory = null;
			highlights = null;
			absences = null;
		}

		//Initialize GitHub API
		if (credentials.get("GITHUB_TOKEN") != null && config.getGithubRepository() != null) github = new GitHubAPI(credentials.get("GITHUB_TOKEN"));
		else {
			logger.warn("GitHub api disabled due to missing credentials");
			github = null;
		}

		//Initialize Spotify API
		if (credentials.get("SPOTIFY_CLIENT_ID") != null) spotify = new Spotify(credentials.get("SPOTIFY_CLIENT_ID"), credentials.get("SPOTIFY_CLIENT_SECRET"));
		else {
			logger.warn("Spotify api disabled due to missing credentials");
			spotify = null;
		}

		//Initalize Youtube API
		if (config.getYoutube().isPresent()) {
			String[] keys = getCredentialsArray("YOUTUBE_API_KEY");
			if (keys.length > 0) youtube = new Youtube(keys, this, config.getYoutube().get());
			else {
				logger.warn("Youtube api disabled due to missing credentials");
				youtube = null;
			}
		} else {
			logger.warn("Youtube api disabled due to missing config");
			youtube = null;
		}


		//Setup JDA
		JDABuilder builder = JDABuilder.createDefault(credentials.get("DISCORD_TOKEN"))
				//Show startup activity, see #startActivity
				.setStatus(OnlineStatus.IDLE)
				.setActivity(Activity.customStatus("Bot startet..."))

				//Configuration
				.enableIntents(EnumSet.allOf(GatewayIntent.class))
				.setMemberCachePolicy(MemberCachePolicy.ALL)

				//Listeners
				.addEventListeners(this)

				.addEventListeners(new MemberJoinListener(this))
				.addEventListeners(new TimeoutListener(this))
				.addEventListeners(new MessageListener(this))

				.addEventListeners(new StaffMessage(this))
				.addEventListeners(new TeamMeeting(this))

				.addEventListeners(new DataListener(this))
				.addEventListeners(new PingListener())

				.addEventListeners(memberCount = new MemberCount(this))
				.addEventListeners(roleMemberCount = new RoleMemberCount(this));

		//Configure DiscordUtils
		discordUtils = DiscordUtils.create(builder, this)
				.mirrorConsole(config.getLogForwarding().stream().map(LogForwarding::build).toList())
				.useEventManager(null)
				.useUIManager(null)
				.useCommandManager(e -> () -> e, e -> () -> e, manager -> {
					manager.registerOptionParser(new ColorOptionParser());
					manager.registerOptionParser(new IDOptionParser());

					manager.registerCommand(ConfigCommand.class);

					//Register "default" commands
					manager.registerCommand(BonkCommand.class);
					manager.registerCommand(InfoCommand.class);
					manager.registerCommand(BulkAddRoleCommand.class);
					manager.registerCommand(PingCommand.class);

					manager.registerCommand(QuoteCommand.class);
					manager.registerCommand(QuoteMessageCommand.class);

					manager.registerCommand(FdmdsCommand.class);

					manager.registerCommand(AbsenceCommand.class);

					//old mee6 custom commands
					manager.registerCommand(SocialsCommand.class);

					//Register report commands
					if (reports != null) {
						manager.registerCommand(ReportCommand.class);
						manager.registerCommand(UserReportCommand.class);
						manager.registerCommand(MessageReportCommand.class);
						manager.registerCommand(UserReportSlashCommand.class);
					} else logger.warn("Report system disabled due to missing database");

					//Register bug report commands
					if (github != null) {
						manager.registerCommand(BugCommand.class);
						manager.registerCommand(BugContextCommand.class);
						manager.registerCommand(ContributorCommand.class);
					} else logger.warn("Bug reporting disabled due to missing github api");

					//Register leveling commands
					if (database != null && config.getLevel().isPresent()) {
						manager.registerCommand(RankCommand.class);
						manager.registerCommand(CardCommand.class);
						manager.registerCommand(BadgeCommand.class);

						manager.registerCommand(LevelCommand.class);
					} else logger.warn("Level system disabled due to missing database or level config");

					//Register birthday commands
					manager.registerCommand(BirthdayCommand.class);


					manager.registerCommand(HighlightCommand.class);

					/*
					Automatically update comDiscordWrmands
					The parameter function loads the guild configuration and provides it as cache to all commands.
					Tish way, the configuration does not have to be reloaded for every registration check.

					This cache is also automatically passed to all future calls to updateGuildCommands
					 */
					manager.updateCommands(g -> new Cache().put("config", loadGuild(g)));
				})
				.useListManager(manager -> manager.setPageOption(new OptionData(OptionType.INTEGER, "seite", "Startseite").setMinValue(1)))
				.useCustomRequests(manager -> {
					if (github != null) github.init(manager);
				})
				.build();


		jda = discordUtils.getJDA();
	}

	@NotNull
	public Logger getLogger() {
		return logger;
	}

	public void updateGuildCommands(@NotNull Guild guild) {
		discordUtils.getCommandManager().updateGuildCommands(guild).queue();
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		startActivity();

		//To avoid conflicts, the spotify listener is only started after jda finished loading
		if (spotify != null) {
			if (config.getSpotify().isPresent()) {
				if (database != null) new SpotifyAlert(this);
				else logger.warn("Spotify alerts disabled deu to missing database");
			} else logger.warn("Spotify alerts disabled due to missing configuration");
		}

		//Register Listeners
		//JEvent.getDefaultManager().registerListenerPackage(botPackage);
		EventManager manager = JEvent.getDefaultManager();
		manager.registerListener(DataListener.class);
		manager.registerListener(LevelUpListener.class);
		manager.registerListener(YoutubeListener.class);
		manager.registerListener(TimeoutListener.class);
		manager.registerListener(BirthdayListener.class);
		manager.registerListener(MessageListener.class);

		//Start handlers
		new HolidayAlert(this);
		new BirthdayAlert(this);

		new AbsenceScheduler(this);

		if (youtube != null) youtube.startListener();
	}

	private void startActivity() {
		jda.getPresence().setStatus(OnlineStatus.ONLINE);

		List<Activity> activities = config.getActivity().activities.stream()
				.map(ActivityConfig.ActivityEntry::build)
				.toList();

		//Schedule random activity updates
		executor.scheduleAtFixedRate(
				() -> jda.getPresence().setActivity(activities.get(Main.random.nextInt(activities.size()))),
				0, config.getActivity().interval, TimeUnit.SECONDS
		);
	}

	@Override
	public void onGuildReady(GuildReadyEvent event) {
		//Send startup message
		loadGuild(event.getGuild()).getLogChannel().ifPresent(channel ->
				channel.sendMessageEmbeds(
						new EmbedBuilder()
								.setTitle("Bot wurde gestartet")
								.setDescription("Der Bot hat sich mit der DiscordAPI (neu-) verbunden")
								.addField("Version", BuildInfo.version, true)
								.setColor(getColor(event.getGuild()))
								.setTimestamp(Instant.now())
								.build()
				).queue()
		);
	}

	public void scheduleDaily(int hour, @NotNull Runnable task) {
		long day = TimeUnit.DAYS.toSeconds(1);
		long initialDelay = ZonedDateTime.now(Main.timezone)
				.withHour(hour)
				.withMinute(0)
				.withSecond(0)
				.toEpochSecond() - (System.currentTimeMillis() / 1000);

		if (initialDelay < 0) initialDelay += day;

		executor.scheduleAtFixedRate(() -> {
			try {
				task.run();
			} catch (Exception e) {
				logger.error("An error occurred in daily task", e);
			}
		}, initialDelay, day, TimeUnit.SECONDS);
	}

	@NotNull
	public GuildConfig loadGuild(long guild) {
		return GuildConfig.readFromFile(this, guild);
	}

	@NotNull
	public GuildConfig loadGuild(@NotNull Guild guild) {
		return loadGuild(guild.getIdLong());
	}

	@NotNull
	public Color getColor(long guild) {
		return Optional.ofNullable(loadGuild(guild).getColor()).orElseGet(() -> Color.decode(config.getDefaultColor()));
	}

	@NotNull
	public Color getColor(@Nullable Guild guild) {
		return Optional.ofNullable(guild)
				.map(g -> loadGuild(g).getColor())
				.or(() -> Optional.ofNullable(ColorUtil.parseColor(config.getDefaultColor())))
				.orElseThrow(); //Only happens if neither guild nor main config have valid configuration
	}

	//TODO: Add possibility to call this method to gracefully shutdown the bot
	public void shutdown() {
		try {
			logger.info("Shutting down...");

			executor.shutdownNow();

			jda.shutdown();
			if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
				jda.shutdownNow();
				jda.awaitShutdown();
			}
		} catch (Exception e) {
			logger.error("Regular shutdown failed, forcing shutdown...", e);
			System.exit(1);
		}
	}

	@NotNull
	public static UserSnowflake getUser(@NotNull MessageEmbed embed) {
		return UserSnowflake.fromId(embed.getAuthor().getIconUrl().split("/")[4]); //Avatar Pattern: "https://cdn.discordapp.com/avatars/%s/%s.%s"
	}

	@NotNull
	public static Guild getGuild(@NotNull ISnowflake obj) {
		if (obj instanceof Role r) return r.getGuild();
		else if (obj instanceof Member m) return m.getGuild();

		throw new RuntimeException();
	}

	@NotNull
	public <T> RestAction<T> wrap(@NotNull T value) {
		return new CompletedRestAction<>(jda, value);
	}

	@NotNull
	public String[] getCredentialsArray(String name) {
		return credentials.entries().stream()
				.filter(e -> e.getKey().matches(name + "_\\d+"))
				.map(e -> Map.entry(Integer.parseInt(e.getKey().split("_(?=\\d+$)")[1]), e.getValue()))
				.filter(e -> e.getKey() != -1)
				.sorted(Comparator.comparingInt(Map.Entry::getKey))
				.map(Map.Entry::getValue)
				.toArray(String[]::new);

	}
}
