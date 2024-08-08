package de.slimecloud.slimeball.features.birthday;

import de.slimecloud.slimeball.features.birthday.event.BirthdayEndEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdayStartEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class BirthdayAlert {
	private final SlimeBot bot;

	public BirthdayAlert(@NotNull SlimeBot bot) {
		this.bot = bot;
		bot.scheduleDaily(0, this::check);
	}

	private void check() {
		bot.getJda().getGuilds().forEach(guild -> bot.loadGuild(guild).getBirthday().ifPresent(config -> {
			List<? extends UserSnowflake> members = config.getBirthdayRole().map(guild::getMembersWithRoles).orElse(Collections.emptyList());

			bot.getBirthdays().getAll(guild, members).stream()
					.filter(b -> !b.isBirthday())
					.forEach(b -> new BirthdayEndEvent(b).callEvent());

			bot.getBirthdays().getToday(guild).stream()
					.filter(b -> !members.contains(b.getUser()))
					.forEach(b -> new BirthdayStartEvent(b).callEvent());
		}));
	}
}
