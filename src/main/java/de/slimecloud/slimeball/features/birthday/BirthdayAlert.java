package de.slimecloud.slimeball.features.birthday;

import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.features.birthday.event.BirthdayEndEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdayStartEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.TimeUtil;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
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
			List<Member> members = config.getBirthday().flatMap(BirthdayConfig::getBirthdayRole).map(g::getMembersWithRoles).orElse(Collections.emptyList());

			bot.getBirthdays().getAll(g, members).stream()
					.filter(b -> !TimeUtil.isSameDay(b.getTime(), Instant.now()))
					.forEach(b -> new BirthdayEndEvent(b).callEvent());
		});

		bot.getBirthdays().getToday().forEach(b -> new BirthdayStartEvent(b).callEvent());
	}
}
