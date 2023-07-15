package com.slimebot.alerts.holidays;

import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import de.mineking.discord.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HolidayAlert implements Runnable {
	public final static Logger logger = LoggerFactory.getLogger(HolidayAlert.class);

	public final static String apiHost = "https://ferien-api.de";
	public final static Route apiRoute = Route.get("api/v1/holidays");

	private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public HolidayAlert() {
		Main.scheduleDaily(6, this);
	}

	@Override
	public void run() {
		retrieveHolidays(LocalDate.now().format(formatter))
				.flatMap(this::sendMessage)
				.queue();
	}

	private RestAction<?> sendMessage(List<Holiday> holidays) {
		if(holidays.isEmpty()) {
			return new CompletedRestAction<>(Main.jdaInstance, null);
		}

		return Utils.accumulate(Main.jdaInstance, Main.jdaInstance.getGuilds().stream()
				.map(guild -> (GuildMessageChannel) Main.database.getChannel(guild, DatabaseField.GREETINGS_CHANNEL))
				.filter(Objects::nonNull)
				.map(channel -> {
					String states = holidays
							.stream()
							.map(h -> h.name.split(" ")[1].toUpperCase())
							.collect(Collectors.joining(", "))
							.replaceFirst(",(?=[^,]*$)", " und");

					if(holidays.size() > 3) states = holidays.size() + " Bundesländern";

					return channel.sendMessageEmbeds(
							new EmbedBuilder()
									.setColor(Main.database.getColor(channel.getGuild()))
									.setTitle("ENDLICH FERIEN")
									.setDescription("**Alle Schüler aus " + states + " haben endlich Ferien!**\nGenießt die Ferien solange sie noch sind...")
									.setImage("https://cdn.discordapp.com/attachments/1098707158750724186/1125467211847454781/Slimeferien.png")
									.build()
					);
				})
				.toList()
		);
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
