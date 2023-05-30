package com.slimebot.utils;

import com.slimebot.main.Main;

import java.io.*;
import java.util.Properties;

public class Config {

    public static String botPath = Config.getLocalProperty("config.properties", "main.path") + "/" + Config.getLocalProperty("config.properties", "main.name") + "/";

    public static void createFileWithDir(String configName, String botID, boolean init) throws IOException {
        String directory = botPath;
        File dir = new File(directory + botID);
        if (!dir.exists()) dir.mkdirs();

        File config = new File(directory + botID + "/" + configName + ".yml");
        if (!config.exists()) {
            config.createNewFile();
            if (init) {
                changeProperty(config.getAbsolutePath(), "logChannel", "0");
                changeProperty(config.getAbsolutePath(), "blocklist", "[]");
                changeProperty(config.getAbsolutePath(), "staffRoleId", "0");
                changeProperty(config.getAbsolutePath(), "verifyRoleID", "0");
                changeProperty(config.getAbsolutePath(), "punishmentChannelID", "0");
                changeProperty(config.getAbsolutePath(), "embedColor.rgb.red", "86");
                changeProperty(config.getAbsolutePath(), "embedColor.rgb.green", "157");
                changeProperty(config.getAbsolutePath(), "embedColor.rgb.blue", "60");
            }
        }
    }

    public static void changeProperty(String filename, String key, String value) {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        prop.setProperty(key, value);
        try {
            prop.store(new FileOutputStream(filename), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeProperty(String filename, String key) {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        prop.remove(key);
        try {
            prop.store(new FileOutputStream(filename), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String filename, String key) {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return prop.getProperty(key);
    }

    public static String getLocalProperty(String filename, String key) {
        String property = "error";
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(filename)) {
            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return property;
            }

            prop.load(input);
            property = prop.getProperty(key);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return property;
    }

}
