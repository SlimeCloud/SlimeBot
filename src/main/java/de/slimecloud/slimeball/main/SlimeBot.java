package de.slimecloud.slimeball.main;

import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.commands.Cache;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.javautils.database.DatabaseManager;
import de.slimecloud.slimeball.config.ActivityConfig;
import de.slimecloud.slimeball.config.Config;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.LogForwarding;
import de.slimecloud.slimeball.config.commands.ConfigCommand;
import de.slimecloud.slimeball.features.alerts.HolidayAlert;
import de.slimecloud.slimeball.features.alerts.Spotify;
import de.slimecloud.slimeball.features.alerts.SpotifyAlert;
import de.slimecloud.slimeball.features.birthday.Birthday;
import de.slimecloud.slimeball.features.birthday.BirthdayAlert;
import de.slimecloud.slimeball.features.birthday.BirthdayListener;
import de.slimecloud.slimeball.features.birthday.BirthdayTable;
import de.slimecloud.slimeball.features.birthday.commands.BirthdayCommand;
import de.slimecloud.slimeball.features.fdmds.FdmdsCommand;
import de.slimecloud.slimeball.features.general.BonkCommand;
import de.slimecloud.slimeball.features.general.BulkAddRoleCommand;
import de.slimecloud.slimeball.features.general.InfoCommand;
import de.slimecloud.slimeball.features.general.PingCommand;
import de.slimecloud.slimeball.features.github.BugCommand;
import de.slimecloud.slimeball.features.github.BugContextCommand;
import de.slimecloud.slimeball.features.github.ContributorCommand;
import de.slimecloud.slimeball.features.github.GitHubAPI;
import de.slimecloud.slimeball.features.level.Level;
import de.slimecloud.slimeball.features.level.LevelTable;
import de.slimecloud.slimeball.features.level.card.*;
import de.slimecloud.slimeball.features.level.commands.LeaderboardCommand;
import de.slimecloud.slimeball.features.level.commands.LevelCommand;
import de.slimecloud.slimeball.features.moderation.AutodeleteListener;
import de.slimecloud.slimeball.features.moderation.MemberJoinListener;
import de.slimecloud.slimeball.features.moderation.TimeoutListener;
import de.slimecloud.slimeball.features.poll.Poll;
import de.slimecloud.slimeball.features.poll.PollCommand;
import de.slimecloud.slimeball.features.poll.PollTable;
import de.slimecloud.slimeball.features.quote.QuoteCommand;
import de.slimecloud.slimeball.features.quote.QuoteMessageCommand;
import de.slimecloud.slimeball.features.reminder.RemindCommand;
import de.slimecloud.slimeball.features.reminder.RemindManager;
import de.slimecloud.slimeball.features.reminder.Reminder;
import de.slimecloud.slimeball.features.reminder.ReminderTable;
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
import de.slimecloud.slimeball.features.statistic.MemberCount;
import de.slimecloud.slimeball.features.statistic.RoleMemberCount;
import de.slimecloud.slimeball.features.wrapped.DataListener;
import de.slimecloud.slimeball.features.wrapped.WrappedData;
import de.slimecloud.slimeball.features.wrapped.WrappedDataTable;
import de.slimecloud.slimeball.main.extensions.ColorOptionParser;
import de.slimecloud.slimeball.main.extensions.ColorTypeMapper;
import de.slimecloud.slimeball.main.extensions.IDOptionParser;
import de.slimecloud.slimeball.main.extensions.SnowflakeTypeMapper;
import de.slimecloud.slimeball.util.ColorUtil;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class SlimeBot extends ListenerAdapter {
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);

	private final Config config;
	private final Dotenv credentials;

	private final JDA jda;
	private final DiscordUtils<SlimeBot> discordUtils;

	private final DatabaseManager database;

	private final ReportTable reports;
	private final ReportBlockTable reportBlocks;

	private final PollTable polls;

	private final ReminderTable reminder;
	private RemindManager remindManager;

	private final LevelTable level;
	private final CardDataTable profileData;
	private final GuildCardTable cardProfiles;
	private final CardDecorationTable cardDecorations;

	private final WrappedDataTable wrappedData;

	private final BirthdayTable birthdays;

	private final GitHubAPI github;
	private final Spotify spotify;

	private final MemberCount memberCount;
	private final RoleMemberCount roleMemberCount;

	public SlimeBot(@NotNull Config config, @NotNull Dotenv credentials) throws IOException {
		this.config = config;
		this.credentials = credentials;

		//Verify token
		String token = credentials.get("DISCORD_TOKEN");
		if (token == null) throw new IllegalArgumentException("No token specified");

		//Initialize database if configured
		String databaseHost = credentials.get("DATABASE_HOST");
		if (databaseHost != null) {
			database = new DatabaseManager("jdbc:postgresql://" + databaseHost, credentials.get("DATABASE_USER"), credentials.get("DATABASE_PASSWORD"));
			database.getDriver().installPlugin(new PostgresPlugin());

			database.putData("bot", this);
			database.addMapper(new SnowflakeTypeMapper());
			database.addMapper(new ColorTypeMapper());

			//Initialize tables
			reports = (ReportTable) database.getTable(ReportTable.class, Report.class, () -> new Report(this), "reports").createTable();
			reportBlocks = (ReportBlockTable) database.getTable(ReportBlockTable.class, ReportBlock.class, ReportBlock::new, "report_blocks").createTable();

			polls = (PollTable) database.getTable(PollTable.class, Poll.class, () -> new Poll(this), "polls").createTable();

			reminder = (ReminderTable) database.getTable(ReminderTable.class, Reminder.class, () -> new Reminder(this), "reminders").createTable();

			level = (LevelTable) database.getTable(LevelTable.class, Level.class, () -> new Level(this), "levels").createTable();
			profileData = (CardDataTable) database.getTable(CardDataTable.class, CardProfileData.class, () -> new CardProfileData(this), "card_data").createTable();
			cardProfiles = (GuildCardTable) database.getTable(GuildCardTable.class, GuildCardProfile.class, () -> new GuildCardProfile(this), "guild_card_profiles").createTable();
			cardDecorations = (CardDecorationTable) database.getTable(CardDecorationTable.class, UserCardDecoration.class, () -> new UserCardDecoration(this), "guild_card_decorations").createTable();

			wrappedData = (WrappedDataTable) database.getTable(WrappedDataTable.class, WrappedData.class, () -> new WrappedData(this), "wrapped_data").createTable();

			birthdays = (BirthdayTable) database.getTable(BirthdayTable.class, Birthday.class, () -> new Birthday(this), "birthdays").createTable();
		} else {
			logger.warn("Database credentials missing! Some features will be disabled!");

			database = null;
			reports = null;
			reportBlocks = null;
			polls = null;
			reminder = null;
			level = null;
			profileData = null;
			cardProfiles = null;
			cardDecorations = null;
			wrappedData = null;
			birthdays = null;
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
				.addEventListeners(new AutodeleteListener(this))

				.addEventListeners(new StaffMessage(this))
				.addEventListeners(new TeamMeeting(this))

				.addEventListeners(new DataListener(this))

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

					manager.registerCommand(new Command<>(manager, "test") {
						@Override
						public void performCommand(@NotNull ICommandContext context) throws Exception {
							throw new IOException();
						}
					});

					manager.registerCommand(ConfigCommand.class);

					//Register "default" commands
					manager.registerCommand(BonkCommand.class);
					manager.registerCommand(InfoCommand.class);
					manager.registerCommand(BulkAddRoleCommand.class);
					manager.registerCommand(PingCommand.class);

					manager.registerCommand(QuoteCommand.class);
					manager.registerCommand(QuoteMessageCommand.class);

					manager.registerCommand(FdmdsCommand.class);

					//Register remind commands
					if (reminder != null) {
						manager.registerCommand(RemindCommand.class);
					} else logger.warn("Reminders disabled due to missing database");

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

					//Register poll commands
					if (polls != null) {
						manager.registerCommand(PollCommand.class);
					} else logger.warn("Poll system disabled due to missing database");

					//Register leveling commands
					if (database != null && config.getLevel().isPresent()) {
						manager.registerCommand(RankCommand.class);
						manager.registerCommand(CardCommand.class);
						manager.registerCommand(DecorationCommand.class);

						manager.registerCommand(LeaderboardCommand.class);
						manager.registerCommand(LevelCommand.class);
					} else logger.warn("Level system disabled due to missing database or level config");

					//Register birthday commands
					manager.registerCommand(BirthdayCommand.class);

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

		// Initialize RemindMe manger
		if (reminder != null) {
			remindManager = new RemindManager(this);
			remindManager.scheduleNextReminder();
		}

		new HolidayAlert(this);
		new BirthdayAlert(this);
		new BirthdayListener(this);
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
		long initialDelay = Instant.now().atOffset(ZoneOffset.UTC)
				.withHour(hour)
				.withMinute(0)
				.withSecond(0)
				.toEpochSecond();

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
}
