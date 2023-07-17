package com.slimebot.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class Config {
	public final static Logger logger = LoggerFactory.getLogger(Config.class);
	private static final Dotenv dotenv = Dotenv.load();

	public static String getEnvKey(String key) {
		return dotenv.get(key.toUpperCase());
	}

	public static YamlFile getConfig(String guildID, String configName) {
		return new YamlFile("Slimebot/" + guildID + "/" + configName + ".yml");
	}

	public static void addNewConfig(String configName, String guildID) {
		YamlFile newConfig = getConfig(guildID, configName);

		if(newConfig.exists()) {
			logger.error("Config konnte nicht erstellt werden! {} existiert bereits!", newConfig.getFilePath());
		}

		else {
			try {
				newConfig.createNewFile();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}

			logger.info("Neue config erstellt bei {}", newConfig.getFilePath());
		}
	}

	public static void createMain(String guildID) {
		YamlFile mainConfig = getConfig(guildID, "mainConfig");

		try {
			if(!mainConfig.exists()) {
				mainConfig.createNewFile();
				logger.info("Neue Datei erstellt: {}; Generiere standard Werte...", mainConfig.getFilePath());
			}

			else {
				return;
			}

			mainConfig.load();
		} catch(final Exception e) {
			logger.error("Config konnte nicht erstellt werden", e);
		}

		mainConfig.set("logChannel", 0);
		mainConfig.set("fdmdsChannel", 0);
		mainConfig.set("fdmdsLogChannel", 0);
		mainConfig.set("fdmdsRoleId", 0);
		mainConfig.set("greetingsChannel", 0);
		mainConfig.set("blocklist", Arrays.asList("123456", "7891021"));
		mainConfig.set("staffRoleID", 0);
		mainConfig.set("punishmentChannelID", 0);
		mainConfig.set("embedColor.red", 86);
		mainConfig.set("embedColor.green", 157);
		mainConfig.set("embedColor.blue", 60);

		mainConfig.options().headerFormatter()
				.prefixFirst("######################")
				.commentPrefix("##  ")
				.commentSuffix("  ##")
				.suffixLast("######################");
		mainConfig.setHeader("SlimeBot Config");

		mainConfig.setComment("logChannel", "Default logging Channel ID e.g. 2309845209845202");
		mainConfig.setComment("fdmdsChannel", "Default fdmds Channel ID");
		mainConfig.setComment("fdmdsLogChannel", "Default fdmds Log Channel ID");
		mainConfig.setComment("greetingsChannel", "Default greetings Channel ID");
		mainConfig.setComment("blocklist", "Users who a blocked from creating Reports");
		mainConfig.setComment("staffRoleID", "ID From the Staff Role");
		mainConfig.setComment("punishmentChannelID", "Channel ID from where things like the Timeouts were logged");
		mainConfig.setComment("embedColor", "Default RGB-Color code from Embeds");

		try {
			mainConfig.save();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		logger.info("Config für {} erstellt", guildID);
	}

	public static String getBotInfo(String probPath) {
		YamlFile botConfig = new YamlFile("Slimebot/main/botConfig.yml");

		if(!botConfig.exists()) {
			try {
				botConfig.createNewFile(true);
				botConfig.load();
				botConfig.set("name", "SlimeBot");
				botConfig.set("activity.type", "PLAYING");
				botConfig.set("activity.text", "mit Slimebällen");
				botConfig.save();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			botConfig.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		return botConfig.getString(probPath);
	}
}
