package com.slimebot.alerts.holidays;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HolidayAlert implements Runnable {
	private final URL apiUrl;
	private final CloseableHttpClient httpClient = HttpClients.createDefault();

	public HolidayAlert(URL apiURL) {
		this.apiUrl = apiURL;

		run();
		Main.scheduleDaily(6, this);
	}

	@Override
	public void run() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		try {
			String date = localDate.format(formatter);
			getObjectsAtDate(date, objects -> {
				if(objects.isEmpty()) return;
				sendMessage(objects);
			});
		} catch(URISyntaxException | IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendMessage(List<JsonObject> objects) {
		for(Guild guild : Main.jdaInstance.getGuilds()) {
			MessageChannel channel = Main.database.getChannel(guild, DatabaseField.GREETINGS_CHANNEL);

			if(channel == null) return;

			String states = objects
					.stream()
					.map(o -> o.get("name").getAsString().split(" ")[1].toUpperCase())
					.collect(Collectors.joining(", "));

			//
			if(objects.size() > 3)states = objects.size() + " Bundesländern";

			channel.sendMessageEmbeds(
					new EmbedBuilder()
							.setColor(Main.database.getColor(guild))
							.setTitle("ENDLICH FERIEN")
							.setDescription("**Alle Schüler aus " + states + " haben endlich Ferien!**\nGenießt die Ferien solange sie noch sind...")
							.setImage("https://cdn.discordapp.com/attachments/1098707158750724186/1125467211847454781/Slimeferien.png")
							.build()
			).queue();
		}
	}


	// date = yyyy-MM-dd
	private void getObjectsAtDate(String date, Consumer<List<JsonObject>> consumer) throws URISyntaxException, IOException, ParseException {
		HttpGet request = new HttpGet(apiUrl.toURI());

		httpClient.execute(request, response -> {

			HttpEntity entity = response.getEntity();

			JsonArray array = JsonParser.parseString(EntityUtils.toString(entity)).getAsJsonArray();

			List<JsonObject> jsonObjects = IntStream
					.range(0, array.size())
					// get only the holidays at the right date
					.filter(i -> array.get(i).getAsJsonObject().get("start").getAsString().equalsIgnoreCase(date))
					// get only "real" holidays
					.filter(i -> !(array.get(i).getAsJsonObject().get("name").getAsString().contains("(")))
					// map to JsonObject
					.mapToObj(i -> array.get(i).getAsJsonObject())
					.collect(Collectors.toList());

			consumer.accept(jsonObjects);
			return response;
		});
	}
}
