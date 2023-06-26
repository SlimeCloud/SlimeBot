package com.slimebot.utils;

import com.slimebot.main.Main;
import org.simpleyaml.configuration.file.YamlFile;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.Arrays;

public class Config {

    private static final Dotenv dotenv = Dotenv.load();

    public static String getEnvKey(String key) {
        return dotenv.get(key.toUpperCase());
    }

    public static YamlFile getConfig(String guildID, String configName){
        return new YamlFile("Slimebot/"+guildID+"/"+configName+".yml");
    }





    public static void addNewConfig(String configName, String guildID){
        YamlFile newConfig = getConfig(guildID, configName);

        if (newConfig.exists()){
            System.out.println("\n[ERROR] Can't create Config!\n"+newConfig.getFilePath() + " exists already!\n");
        } else {
            try {
                newConfig.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("[SUCCESS] New Config created at "+newConfig.getFilePath());

        }
    }

    public static void createMain(String guildID){


        YamlFile mainConfig = getConfig(guildID, "mainConfig");

        try {
            if (!mainConfig.exists()) {
                mainConfig.createNewFile();
                System.out.println("New file has been created: " + mainConfig.getFilePath() + "\nGenerate default property...");
            } else {
                return;
            }
            mainConfig.load();
        } catch (final Exception e) {

            e.printStackTrace();
        }

        mainConfig.set("logChannel", 0);
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
        mainConfig.setComment("blocklist", "Users who a blocked from creating Reports");
        mainConfig.setComment("staffRoleID", "ID From the Staff Role");
        mainConfig.setComment("punishmentChannelID", "Channel ID from where things like the Timeouts were logged");
        mainConfig.setComment("embedColor", "Default RGB-Color code from Embeds");


        try {
            mainConfig.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Main Config for " + guildID + " successfully created");

    }

    public static String getBotInfo(String probPath){
        YamlFile botConfig = new YamlFile("Slimebot/main/botConfig.yml");
        if (!(botConfig.exists())){
            try {
                botConfig.createNewFile(true);
                botConfig.load();
                botConfig.set("name", "SlimeBot");
                botConfig.set("version", "2.0");
                botConfig.set("activity.type", "PLAYING");
                botConfig.set("activity.text", "mit Slimebällen");
                botConfig.set("token.main", "");
                botConfig.set("token.test", "");
                botConfig.save();
                Main.missingToken();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            botConfig.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return botConfig.getString(probPath);
    }


}
