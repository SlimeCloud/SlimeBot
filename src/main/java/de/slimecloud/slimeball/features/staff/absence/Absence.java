package de.slimecloud.slimeball.features.staff.absence;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class Absence implements DataClass<Absence> {
	private final SlimeBot bot;

	@Column(key = true)
	private UserSnowflake teamMember;

	@Column
	private final Guild guild;

	@Column
	private Instant time;

	public Absence(SlimeBot bot) {
		this(bot, null, null, null);
	}

	@NotNull
	@Override
	public Table<Absence> getTable() {
		return bot.getAbsences();
	}

	public @Nullable Runnable check() {
		List<Absence> absences = bot.getAbsences().getExpiredAbsence(ZonedDateTime.now(Main.timezone).toInstant());

		if (absences.isEmpty()) return null;

		absences.forEach(absence -> {
			bot.loadGuild(absence.getGuild()).getAbsenceRole().ifPresent(role -> absence.getGuild().removeRoleFromMember(absence.getTeamMember(), role).queue());
			bot.getAbsences().remove(absence);

			bot.loadGuild(absence.getGuild()).getLogChannel().ifPresent(channel -> channel.sendMessageEmbeds(new EmbedBuilder()
					.setTitle(":information_source:  Abwesenheit geupdatet")
					.setColor(bot.getColor(absence.getGuild()))
					.setDescription(absence.getTeamMember().getAsMention() + " ist nun wieder Anwesend!")
					.setTimestamp(Instant.now())
					.build()).queue()
			);
		});
		return null;
	}
}
