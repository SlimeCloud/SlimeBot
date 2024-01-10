package de.slimecloud.slimeball.features.birthday;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.JEvent;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.features.birthday.event.BirthdayEndEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdayRemoveEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdaySetEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdayStartEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.TimeUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class BirthdayListener {
	private final SlimeBot bot;

	public BirthdayListener(@NotNull SlimeBot bot) {
		this.bot = bot;
		JEvent.getDefaultManager().registerListener(this);
	}

	@EventHandler
	public void onBirthdaySet(@NotNull BirthdaySetEvent event) {
		int age = event.getNewBirthday().getAge();

		if (age != -1 && age < 16) {
			Member member = event.getMember();
			Guild guild = member.getGuild();

			BirthdayConfig config = bot.loadGuild(guild).getBirthday().orElse(null);
			if (config == null) return;

			TextChannel channel = guild.getTextChannelById(config.reportChat);
			if (channel != null)
				channel.sendMessage(String.format("%s (%s) hat gerade seinen/ihren Geburtstag auf den %s gesetzt und ist somit erst **%s** Jahre alt!", member.getAsMention(), member.getEffectiveName(), event.getNewBirthday(), age)).queue();
		}

		if (TimeUtil.isSameDay(event.getNewBirthday().getTime(), Instant.now(), true)) new BirthdayStartEvent(event.getNewBirthday()).callEvent();
	}

	@EventHandler
	public void onBirthdayRemove(@NotNull BirthdayRemoveEvent event) {
		//Call the method instead of the event, as it will be called in any case, even if the user does not have a birthday and has just removed their birthday.
		//But in the onBirthdayEnd method it does not matter, as it only tries to remove the birthday role.
		onBirthdayEnd(new BirthdayEndEvent(event.getMember()));
	}


	@EventHandler
	public void onBirthdayEnd(@NotNull BirthdayEndEvent event) {
		bot.loadGuild(event.getGuild()).getBirthday().flatMap(BirthdayConfig::getBirthdayRole).ifPresent(role -> event.getGuild().removeRoleFromMember(event.getMember(), role).queue());
	}

	@EventHandler
	public void onBirthdayStart(@NotNull BirthdayStartEvent event) {
		GuildConfig guildConfig = bot.loadGuild(event.getGuild());

		guildConfig.getBirthday().flatMap(BirthdayConfig::getBirthdayRole).ifPresent(role -> event.getGuild().addRoleToMember(event.getMember(), role).queue());

		guildConfig.getGreetingsChannel().ifPresent(channel -> {
			int age = event.getBirthday().getAge();
			channel.sendMessage(event.getMember().getAsMention() + " hat heute Geburtstag" + (age != -1 ? String.format(" und wird %s Jahre alt", age) : "") + " :birthday: :partying_face:").queue();
		});
	}
}
