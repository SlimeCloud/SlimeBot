package de.slimecloud.slimeball.features.birthday;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.Listener;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.features.birthday.event.BirthdayEndEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdayRemoveEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdaySetEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdayStartEvent;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

@Listener
@RequiredArgsConstructor
public class BirthdayListener {
	private final SlimeBot bot;

	@EventHandler
	public void onBirthdaySet(@NotNull BirthdaySetEvent event) {
		if (event.getNewBirthday().isBirthday(ZonedDateTime.now(Main.timezone))) {
			new BirthdayStartEvent(event.getNewBirthday()).callEvent();
		}
	}

	@EventHandler
	public void onBirthdayRemove(@NotNull BirthdayRemoveEvent event) {
		bot.loadGuild(event.getMember().getGuild()).getBirthday()
				.flatMap(BirthdayConfig::getBirthdayRole)
				.ifPresent(role -> {
					if (event.getMember().getRoles().contains(role)) {
						new BirthdayEndEvent(event.getMember()).callEvent();
					}
				});
	}


	@EventHandler
	public void onBirthdayEnd(@NotNull BirthdayEndEvent event) {
		bot.loadGuild(event.getGuild()).getBirthday()
				.flatMap(BirthdayConfig::getBirthdayRole)
				.ifPresent(role -> event.getGuild().removeRoleFromMember(event.getMember(), role).queue());
	}

	@EventHandler
	public void onBirthdayStart(@NotNull BirthdayStartEvent event) {
		GuildConfig config = bot.loadGuild(event.getGuild());

		config.getBirthday()
				.flatMap(BirthdayConfig::getBirthdayRole)
				.ifPresent(role -> event.getGuild().addRoleToMember(event.getMember(), role).queue());

		config.getGreetingsChannel().ifPresent(channel -> channel
				.sendMessage(event.getMember().getAsMention() + " hat heute Geburtstag! :birthday: :partying_face:")
				.queue()
		);
	}
}
