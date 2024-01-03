package de.slimecloud.slimeball.config;

import de.slimecloud.slimeball.config.engine.CategoryInfo;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.config.engine.Info;
import de.slimecloud.slimeball.features.alerts.SpotifyNotificationConfig;
import de.slimecloud.slimeball.features.fdmds.FdmdsConfig;
import de.slimecloud.slimeball.features.level.GuildLevelConfig;
import de.slimecloud.slimeball.features.moderation.AutodeleteFlag;
import de.slimecloud.slimeball.features.staff.MeetingConfig;
import de.slimecloud.slimeball.features.staff.StaffConfig;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.ColorUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.*;

@Slf4j
@CategoryInfo(name = "Standard", command = "general", description = "Generelle Konfiguration des Servers")
@ToString
public class GuildConfig {
	@NotNull
	public static GuildConfig readFromFile(@NotNull SlimeBot bot, long guild) {
		String path = bot.getConfig().getGuildStorage().replace("%guild%", String.valueOf(guild));

		try {
			File file = new File(path);

			if (!file.exists()) return new GuildConfig().configure(bot, path, guild);

			try (FileReader reader = new FileReader(file)) {
				return Main.json.fromJson(reader, GuildConfig.class).configure(bot, path, guild);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@ToString.Exclude
	private transient SlimeBot bot;

	private transient long guild;
	private transient String path;

	@Setter
	@ConfigField(name = "Farbe", command = "color", description = "Die primäre Farbe des Servers", type = ConfigFieldType.COLOR)
	private String color;

	@Setter
	@ConfigField(name = "Team", command = "team", description = "Die Team-Rolle", type = ConfigFieldType.ROLE)
	private Long teamRole;

	@Getter
	@ConfigField(name = "Beitritts Rollen", command = "autorole", description = "Rollen, die Mitgliedern beim Beitreten gegeben werden", type = ConfigFieldType.ROLE)
	private final List<Long> joinRoles = new ArrayList<>();

	@Setter
	@ConfigField(name = "Gruß-Kanal", command = "greetings", description = "Kanal für Grußnachrichten", type = ConfigFieldType.MESSAGE_CHANNEL)
	private Long greetingsChannel;

	@Setter
	@ConfigField(name = "Zitate-Kanal", command = "quote", description = "Kanal, in dem Zitate gesendet werden", type = ConfigFieldType.MESSAGE_CHANNEL)
	private Long quoteChannel;

	@Setter
	@ConfigField(name = "Log-Kanal", command = "log", description = "Kanal, in dem Log-Nachrichten für den Bot gesendet werden", type = ConfigFieldType.MESSAGE_CHANNEL)
	private Long logChannel;

	@Setter
	@ConfigField(name = "Straf-Kanal", command = "punishment", description = "Kanal, in dem Informationen über Moderations-Handlungen gesendet werden", type = ConfigFieldType.MESSAGE_CHANNEL)
	private Long punishmentChannel;

	@Setter
	@ConfigField(name = "Contributor-Rolle", command = "contributor", description = "Rolle, die Mitglieder erhalten, die am SlimeBall Bot mitgewirkt haben", type = ConfigFieldType.ROLE)
	private Long contributorRole;

	@Setter
	@ConfigField(name = "Automatisches Nachrichtenlöschen", command = "autodelete", description = "Kanäle, in denen Nachrichten automatisch gelöscht werden", type = ConfigFieldType.ENUM)
	@Info(keyType = ConfigFieldType.MESSAGE_CHANNEL)
	private Map<Long, EnumSet<AutodeleteFlag>> autodelete = new HashMap<>();

	@Setter
	@CategoryInfo(name = "Spotify", command = "spotify", description = "Konfiguration für Spotify-Alerts")
	private SpotifyNotificationConfig spotify;

	@Setter
	@CategoryInfo(name = "FdmdS", command = "fdmds", description = "Konfiguration für \"Frag doch mal den Schleim\"")
	private FdmdsConfig fdmds;

	@Setter
	@CategoryInfo(name = "Level", command = "level", description = "Konfiguration für das Level-System")
	private GuildLevelConfig level;

	@Setter
	@CategoryInfo(name = "Meeting", command = "meeting", description = "Konfiguration für Team-Meetings")
	private MeetingConfig meeting;

	@Setter
	@CategoryInfo(name = "Team-Nachricht", command = "team-message", description = "Kanfigration für die Team-Nachricht")
	private StaffConfig teamMessage;

	@NotNull
	private GuildConfig configure(@NotNull SlimeBot bot, @NotNull String path, long guild) {
		this.bot = bot;
		this.guild = guild;
		this.path = path;

		if (spotify != null) spotify.bot = bot;
		if (fdmds != null) fdmds.bot = bot;
		if (level != null) level.bot = bot;
		if (teamMessage != null) teamMessage.bot = bot;
		if (meeting != null) meeting.bot = bot;

		return this;
	}

	@NotNull
	public SlimeBot getBot() {
		return bot;
	}

	@NotNull
	public Guild getGuild() {
		return bot.getJda().getGuildById(guild);
	}

	@Nullable
	public Color getColor() {
		return color == null || color.isEmpty() ? null : ColorUtil.parseColor(color);
	}

	@NotNull
	public Optional<SpotifyNotificationConfig> getSpotify() {
		return Optional.ofNullable(spotify);
	}

	@NotNull
	public Optional<FdmdsConfig> getFdmds() {
		return Optional.ofNullable(fdmds);
	}

	@NotNull
	public Optional<GuildLevelConfig> getLevel() {
		return Optional.ofNullable(level);
	}

	@NotNull
	public Optional<MeetingConfig> getMeeting() {
		return Optional.ofNullable(meeting);
	}

	@NotNull
	public Optional<StaffConfig> getTeamMessage() {
		return Optional.ofNullable(teamMessage);
	}


	@NotNull
	public Optional<Role> getTeamRole() {
		return Optional.ofNullable(teamRole).map(bot.getJda()::getRoleById);
	}

	@NotNull
	public Optional<GuildMessageChannel> getGreetingsChannel() {
		return Optional.ofNullable(greetingsChannel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}

	@NotNull
	public Optional<GuildMessageChannel> getQuoteChannel() {
		return Optional.ofNullable(quoteChannel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}

	@NotNull
	public Optional<GuildMessageChannel> getLogChannel() {
		return Optional.ofNullable(logChannel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}

	@NotNull
	public Optional<GuildMessageChannel> getPunishmentChannel() {
		return Optional.ofNullable(punishmentChannel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}

	@NotNull
	public Optional<Role> getContributorRole() {
		return Optional.ofNullable(contributorRole).map(bot.getJda()::getRoleById);
	}

	@NotNull
	public Optional<EnumSet<AutodeleteFlag>> getAutodelete(@NotNull Channel channel) {
		return Optional.ofNullable(autodelete).map(a -> a.get(channel.getIdLong()));
	}

	@NotNull
	public GuildConfig save() {
		try {
			File file = new File(path);

			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}

			try (FileWriter writer = new FileWriter(file)) {
				Main.formattedJson.toJson(this, writer);
			}
		} catch (IOException e) {
			logger.error("Failed to write guild config", e);
		}
		return this;
	}
}
