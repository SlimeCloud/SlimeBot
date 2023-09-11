package com.slimebot.alerts.holidays;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HolidayAlert implements Runnable {

    public final static String apiHost = "https://ferien-api.de";
    public final static Route apiRoute = Route.get("api/v1/holidays");

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public HolidayAlert() {
        Main.scheduleDaily(6, this);
    }

    @Override
    public void run() {
        retrieveHolidays(LocalDate.now().format(formatter))
                .queue(this::sendMessage);
    }

    private void sendMessage(List<Holiday> holidays) {
        if (holidays.isEmpty()) return;

        for (Guild guild : Main.jdaInstance.getGuilds()) {
            GuildConfig.getConfig(guild).getGreetingsChannel().ifPresent(channel -> {
                String states = holidays
                        .stream()
                        .map(h -> h.name.split(" ")[1].toUpperCase())
                        .collect(Collectors.joining(", "))
                        .replaceFirst(",(?=[^,]*$)", " und");

                if (holidays.size() > 3) states = holidays.size() + " Bundesländern";

                channel.sendMessageEmbeds(
                        new EmbedBuilder()
                                .setColor(GuildConfig.getColor(guild))
                                .setTitle("ENDLICH FERIEN")
                                .setDescription("**Alle Schüler aus " + states + " haben endlich Ferien!**\nGenießt die Ferien solange sie noch sind...")
                                .setImage("https://cdn.discordapp.com/attachments/1098707158750724186/1125467211847454781/Slimeferien.png")
                                .build()
                ).queue();
            });
        }
    }

    private RestAction<List<Holiday>> retrieveHolidays(String date) {
        return Main.discordUtils.getCustomRestActionManager().request(apiHost, apiRoute.compile(), null, (response, request) ->
                response.getArray().stream(DataArray::getObject)
                        .map(Holiday::new)
                        .filter(h -> h.start().equalsIgnoreCase(date)) // get only the holidays at the right date
                        .filter(h -> !h.name().contains("(")) // get only "real" holidays
                        .peek(h -> logger.info("Ferien erkannt: " + h))
                        .toList()
        );
    }

    public record Holiday(int year, String start, String name, String end, String stateCode, String slug) {
        public Holiday(DataObject data) {
            this(data.getInt("year"), data.getString("start"), data.getString("name"), data.getString("end"), data.getString("stateCode"), data.getString("slug"));
        }
    }

}
