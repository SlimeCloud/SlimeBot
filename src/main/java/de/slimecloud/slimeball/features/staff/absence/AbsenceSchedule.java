package de.slimecloud.slimeball.features.staff.absence;

import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
public class AbsenceSchedule {
	SlimeBot bot;

	public AbsenceSchedule(@NotNull SlimeBot bot) {
		bot.scheduleDaily(12, this::check);
	}

	private void check() {
		logger.info("Check for expired absence");

		List<Absence> absences = bot.getAbsences().getExpiredAbsence(ZonedDateTime.now(Main.timezone).toInstant());

		if (absences.isEmpty()) return;
		logger.info("found {} expired absence", absences.size());

		absences.forEach(absence -> {
			bot.loadGuild(absence.getGuild()).getAbsence().map(AbsenceConfig::getRole).ifPresent(role -> absence.getGuild().removeRoleFromMember(absence.getTeamMember(), role).queue());
			bot.getAbsences().remove(absence);

			bot.loadGuild(absence.getGuild()).getAbsence().flatMap(AbsenceConfig::getChannel).ifPresent(channel -> channel.sendMessageEmbeds(new EmbedBuilder()
					.setTitle(":information_source:  Abwesenheit geupdatet")
					.setColor(bot.getColor(absence.getGuild()))
					.setDescription(absence.getTeamMember().getAsMention() + " ist nun wieder Anwesend!")
					.setTimestamp(Instant.now())
					.build()).queue()
			);
		});
	}
}
