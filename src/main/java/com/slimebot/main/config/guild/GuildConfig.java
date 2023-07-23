package com.slimebot.main.config.guild;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GuildConfig {
	public static final Logger logger = LoggerFactory.getLogger(GuildConfig.class);

	private static final Map<Long, GuildConfig> guildConfig = new HashMap<>();

	public static void load(Guild guild) {
		try {
			File file = new File("guild/" + guild.getIdLong() + ".json");

			if(!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}

			try(Reader reader = new FileReader(file)) {
				GuildConfig config = Main.gson.fromJson(reader, GuildConfig.class);

				if(config == null) {
					config = new GuildConfig();
				}

				config.guild = guild.getIdLong();
				guildConfig.put(guild.getIdLong(), config);
			}
		} catch(Exception e) {
			logger.error("Failed to load config for guild " + guild, e);
		}
	}

	public static GuildConfig getConfig(Guild guild) {
		return guildConfig.get(guild.getIdLong());
	}

	public static Color getColor(Guild guild) {
		return Optional.ofNullable(guild).map(GuildConfig::getConfig).flatMap(GuildConfig::getColor).orElse(Color.decode(Main.config.color));
	}

	public static GuildConfig getConfig(long guild) {
		return guildConfig.get(guild);
	}

	public static Color getColor(long guild) {
		return getConfig(guild).getColor().orElse(Color.decode(Main.config.color));
	}

	public void save() {
		try(Writer writer = new FileWriter("guild/" + guild + ".json")) {
			Main.gson.toJson(this, writer);
		} catch(Exception e) {
			logger.error("Failed to save config for guild " + guild, e);
		}
	}

	private transient long guild;

	public String color;

	public Long logChannel;
	public Long greetingsChannel;
	public Long punishmentChannel;

	public Long contributorRole;

	public Long staffRole;

	public SpotifyNotificationConfig spotify;
	public FdmdsConfig fdmds;
	public StaffConfig staffMessage;

	public Optional<Color> getColor() {
		return Optional.ofNullable(color).map(Color::decode);
	}

	public Optional<GuildMessageChannel> getLogChannel() {
		return getChannel(logChannel);
	}

	public Optional<GuildMessageChannel> getGreetingsChannel() {
		return getChannel(greetingsChannel);
	}

	public Optional<GuildMessageChannel> getPunishmentChannel() {
		return getChannel(punishmentChannel);
	}


	public Optional<Role> getContributorRole() {
		return getRole(contributorRole);
	}

	public Optional<Role> getStaffRole() {
		return getRole(staffRole);
	}


	public Optional<FdmdsConfig> getFdmds() {
		return Optional.ofNullable(fdmds);
	}

	public FdmdsConfig getOrCreateFdmds() {
		return getFdmds().orElseGet(() -> {
			fdmds = new FdmdsConfig();
			return fdmds;
		});
	}

	public Optional<SpotifyNotificationConfig> getSpotify() {
		return Optional.ofNullable(spotify);
	}

	public SpotifyNotificationConfig getOrCreateSpotify() {
		return getSpotify().orElseGet(() -> {
			spotify = new SpotifyNotificationConfig();
			return spotify;
		});
	}

	public Optional<StaffConfig> getStaffConfig() {
		return Optional.ofNullable(staffMessage);
	}

	public StaffConfig getOrCreateStaff() {
		return getStaffConfig().orElseGet(() -> {
			staffMessage = new StaffConfig();
			return staffMessage;
		});
	}

	//Internal helper methods
	static Optional<GuildMessageChannel> getChannel(Long channel) {
		return Optional.ofNullable(channel).map(id -> Main.jdaInstance.getChannelById(GuildMessageChannel.class, id));
	}

	static Optional<Role> getRole(Long role) {
		return Optional.ofNullable(role).map(id -> Main.jdaInstance.getRoleById(id));
	}
}
