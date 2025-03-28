package de.slimecloud.slimeball.features.birthday;

import de.slimecloud.slimeball.features.birthday.event.BirthdayEndEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdayStartEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class BirthdayAlert {
	private final SlimeBot bot;

	public BirthdayAlert(@NotNull SlimeBot bot) {
		this.bot = bot;
		bot.getScheduler().scheduleDaily(0, true, this::check);
	}

	private void check() {
		bot.getJda().getGuilds().forEach(guild -> bot.loadGuild(guild).getBirthday().ifPresent(config -> {
			List<Long> members = config.getBirthdayRole().map(guild::getMembersWithRoles).map(m -> m.stream().map(Member::getIdLong).toList()).orElse(Collections.emptyList());

			bot.getBirthdays().getAll(guild, members).stream()
					.filter(b -> !b.isBirthday())
					.forEach(b -> new BirthdayEndEvent(b).callEvent());

			bot.getBirthdays().getToday(guild).stream()
					.filter(b -> !members.contains(b.getUser().getIdLong()))
					.forEach(b -> new BirthdayStartEvent(b).callEvent());
		}));
	}
}
