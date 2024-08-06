package de.slimecloud.slimeball.features.birthday;

import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.features.birthday.event.BirthdayEndEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdayStartEvent;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

public class BirthdayAlert {
	private final SlimeBot bot;

	public BirthdayAlert(@NotNull SlimeBot bot) {
		this.bot = bot;
		bot.scheduleDaily(6, this::check);
	}

	private void check() {
		bot.getJda().getGuilds().forEach(g -> {
			GuildConfig config = bot.loadGuild(g);
			List<? extends UserSnowflake> members = config.getBirthday().flatMap(BirthdayConfig::getBirthdayRole).map(g::getMembersWithRoles).orElse(Collections.emptyList());

			bot.getBirthdays().getAll(g, members).stream()
					.filter(b -> !b.isBirthday(ZonedDateTime.now(Main.timezone)))
					.forEach(b -> new BirthdayEndEvent(b).callEvent());

			bot.getBirthdays().getAll(g, null).stream()
					.filter(b -> !members.contains(b.getUser()))
					.filter(b -> b.isBirthday(ZonedDateTime.now(Main.timezone)))
					.forEach(b -> new BirthdayStartEvent(b).callEvent());
		});
	}
}
