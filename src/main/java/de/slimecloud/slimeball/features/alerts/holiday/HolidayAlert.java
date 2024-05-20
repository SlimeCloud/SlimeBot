package de.slimecloud.slimeball.features.alerts.holiday;

import de.mineking.discordutils.restaction.HttpHost;
import de.slimecloud.slimeball.features.alerts.holiday.model.Country;
import de.slimecloud.slimeball.features.alerts.holiday.model.SchoolHoliday;
import de.slimecloud.slimeball.features.alerts.holiday.model.Subdivision;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.main.SlimeEmoji;
import io.leangen.geantyref.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class HolidayAlert {
	public final static String API_HOST = "https://openholidaysapi.org/";

	public final static Route COUNTRIES = Route.get("Countries");
	public final static Route SUBDIVISIONS = Route.get("Subdivisions");
	public final static Route HOLIDAYS = Route.get("SchoolHolidays");

	public final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final List<Country> countries;
	private final List<Subdivision> subdivisions;

	private final SlimeBot bot;
	private final HttpHost host;

	public HolidayAlert(@NotNull SlimeBot bot) {
		this.bot = bot;
		this.host = bot.getDiscordUtils().getCustomRestActionManager().createHost(API_HOST);

		this.countries = retrieveCountries().complete();

		//Retrieve subdivisions for all configured countries
		this.subdivisions = countries.stream()
				.flatMap(c -> retrieveSubdivisions(c.getIsoCode()).complete().stream())
				.toList();

		//Check every day at 6 AM
		if (LocalDateTime.now().getHour() >= 6) check();
		bot.scheduleDaily(6, this::check);
	}

	public void check() {
		if (countries.isEmpty()) return;

		String date = formatter.format(LocalDateTime.now());

		//Retrieve holidays for all configured countries
		RestAction.allOf(bot.getConfig().getCountryCodes().stream().map(c -> retrieveHolidays(c, date)).toList()).queue(temp -> {
			//accumulate results
			List<SchoolHoliday> holidays = temp.stream().flatMap(Collection::stream).toList();

			holidays.stream().map(h -> h.getName("DE")).collect(Collectors.toSet()).forEach(name -> {
				List<SchoolHoliday> current = holidays.stream().filter(h -> h.getName("DE").equals(name)).toList();

				//Find regions
				Set<Subdivision> regions = current.stream()
						.flatMap(h -> Arrays.stream(h.getSubdivisions()))
						.map(s -> subdivisions.stream().filter(t -> t.getCode().startsWith(s.getCode())).findFirst())
						.filter(Optional::isPresent)
						.map(Optional::get)
						.collect(Collectors.toSet());

				//Find countries
				Set<Country> countries = regions.stream()
						.map(r -> this.countries.stream().filter(c -> r.getCode().startsWith(c.getIsoCode())).findFirst())
						.filter(Optional::isPresent)
						.map(Optional::get)
						.collect(Collectors.toSet());

				//Build region string
				String regionsString = regions.size() > 5
						? countries.stream().map(c -> c.getName("DE") + " (" + regions.stream().filter(r -> r.getCode().startsWith(c.getIsoCode())).count() + " Regionen)").collect(Collectors.joining(", ")).replaceFirst(",(?=[^,]*$)", " und")
						: regions.stream().map(r -> r.getName("DE")).collect(Collectors.joining(", ")).replaceFirst(",(?=[^,]*$)", " und");

				//SEnd message
				bot.getJda().getGuilds().forEach(g -> bot.loadGuild(g).getGreetingsChannel().ifPresent(c -> c.sendMessageEmbeds(new EmbedBuilder()
						.setColor(bot.getColor(g))
						.appendDescription("## " + name + "\n")
						.appendDescription("Für alle Schüler aus **" + regionsString + "** haben die **" + name + "** begonnen! Genießt die Schulfreie Zeit, solange ihr könnt " + SlimeEmoji.PARTY.toString(g))
						.setImage("https://cdn.discordapp.com/attachments/1098707158750724186/1125467211847454781/Slimeferien.png")
						.build()
				).queue()));
			});
		});
	}

	@NotNull
	private RestAction<List<Country>> retrieveCountries() {
		return host.request(COUNTRIES.compile(), (request, response) -> Main.json.fromJson(response.body().string(), new TypeToken<List<Country>>(){}.getType()));
	}

	@NotNull
	private RestAction<List<Subdivision>> retrieveSubdivisions(@NotNull String country) {
		return host.request(SUBDIVISIONS.compile().withQueryParams("countryIsoCode", country), (request, response) -> Main.json.fromJson(response.body().string(), new TypeToken<List<Subdivision>>(){}.getType()));
	}

	@NotNull
	@SuppressWarnings("unchecked")
	private RestAction<List<SchoolHoliday>> retrieveHolidays(@NotNull String country, @NotNull String date) {
		return host.request(HOLIDAYS.compile().withQueryParams(
						"countryIsoCode", country,
						"validFrom", date,
						"validTo", date
				), (request, response) -> ((List<SchoolHoliday>) Main.json.fromJson(response.body().string(), new TypeToken<List<SchoolHoliday>>() {}.getType())).stream()
						.filter(h -> h.getStartDate().equals(date))
						.toList()
		);
	}
}
