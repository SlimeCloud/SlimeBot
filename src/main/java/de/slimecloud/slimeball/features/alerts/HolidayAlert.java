package de.slimecloud.slimeball.features.alerts;

import de.mineking.discordutils.restaction.HttpHost;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import io.leangen.geantyref.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HolidayAlert {
	public final static String apiHost = "https://ferien-api.de/";
	public final static Route apiRoute = Route.get("api/v1/holidays");

	private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final SlimeBot bot;
	private final HttpHost host;

	public HolidayAlert(@NotNull SlimeBot bot) {
		this.bot = bot;
		this.host = bot.getDiscordUtils().getCustomRestActionManager().createHost(apiHost);

		if (LocalDateTime.now().getHour() >= 6) check();
		//Check every day at 6 AM
		bot.scheduleDaily(6, this::check);
	}

	public void check() {
		retrieveHolidays(formatter.format(LocalDateTime.now())).queue(this::sendMessage);
	}

	private void sendMessage(@NotNull List<Holiday> holidays) {
		if (holidays.isEmpty()) return;

		for (Guild guild : bot.getJda().getGuilds()) {
			bot.loadGuild(guild).getGreetingsChannel().ifPresent(channel -> {
				String states = holidays
						.stream()
						.map(h -> h.name.split(" ")[1].toUpperCase())
						.collect(Collectors.joining(", "))
						.replaceFirst(",(?=[^,]*$)", " und");

				if (holidays.size() > 3) states = holidays.size() + " Bundesländern";

				channel.sendMessageEmbeds(new EmbedBuilder()
						.setTitle("ENDLICH FERIEN")
						.setColor(bot.getColor(guild))
						.setDescription("**Alle Schüler aus " + states + " haben endlich Ferien!**\nGenießt die Ferien solange sie noch sind...")
						.setImage("https://cdn.discordapp.com/attachments/1098707158750724186/1125467211847454781/Slimeferien.png")
						.build()
				).queue();
			});
		}
	}

	@SuppressWarnings("unchecked")
	@NotNull
	private RestAction<List<Holiday>> retrieveHolidays(@NotNull String date) {
		//Make API request using RestAction
		return host.request(apiRoute.compile(), (request, response) -> ((List<Holiday>) Main.json.fromJson(response.body().string(), new TypeToken<List<Holiday>>() {
				}.getType())).stream()
						.filter(h -> h.start().equalsIgnoreCase(date)) //Filter for holidays starting today
						.filter(h -> !h.name().contains("(")) //Ignore "unofficial" holidays
						.peek(h -> logger.info("Found holidays: " + h))
						.toList()
		);
	}

	public record Holiday(int year, String start, String name, String end, String stateCode, String slug) {
	}
}
