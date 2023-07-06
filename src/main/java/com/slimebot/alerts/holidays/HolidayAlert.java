package com.slimebot.alerts.holidays;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import com.slimebot.utils.DailyTask;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HolidayAlert implements Runnable {
    private URL apiUrl;
    private CloseableHttpClient httpClient = HttpClients.createDefault();

    public HolidayAlert(URL apiURL) {
        this.apiUrl = apiURL;
        try {
            Main.getJDAInstance().awaitReady();
            run();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        new DailyTask(6, this);
    }


    @Override
    public void run() {
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            String date = localDate.format(formatter);
            date = date.replace("07-06", "06-22");
            JsonObject object = getObjectAtDate(date);
            if(object == null)return;

            // returns if it's not a "real" holiday
            if(object.get("name").getAsString().contains("("))return;

            sendMessage(object);
        } catch (URISyntaxException | IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(JsonObject object) {

        for(Guild guild : Main.jdaInstance.getGuilds()) {
            System.out.println("aaaaaa");
            TextChannel channel = getChannelFromConfig(guild.getId(), "greetingsChannel");
            if(channel == null)return;
            /*

                TODO: good Message

             */

            // 0: holiday name, 1: state, 2: year
            String[] name = object.get("name").getAsString().split(" ");
            if(name.length < 2)return;

            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Main.embedColor(guild.getId()));
            embed.setTitle("ENDLICH FERIEN");
            embed.setDescription("**Alle Schüler aus " + name[1].toUpperCase() + " haben endlich Ferien!**\r\n" +
                    "Genießt die Ferien solange sie noch sind...");
            embed.setImage("https://cdn.discordapp.com/attachments/1098707158750724186/1125467211847454781/Slimeferien.png");

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }


    // date = yyyy-MM-dd
    private JsonObject getObjectAtDate(String date) throws URISyntaxException, IOException, ParseException {
        HttpGet request = new HttpGet(apiUrl.toURI());

        CloseableHttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();

        JsonArray array = JsonParser.parseString(EntityUtils.toString(entity)).getAsJsonArray();

        response.close();

        for(int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            if(object.get("start").getAsString().equalsIgnoreCase(date))return object;
        }
        return null;
    }

    private TextChannel getChannelFromConfig(String guildId, String path) {
        if(guildId == null || path == null)return null;
        YamlFile config = Config.getConfig(guildId, "mainConfig");
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TextChannel channel;
        try {
            channel = Main.getJDAInstance().getGuildById(guildId).getTextChannelById(config.getString(path));
        } catch (IllegalArgumentException n){
            config.set(path, 0);
            try {
                config.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        return channel;
    }
}
