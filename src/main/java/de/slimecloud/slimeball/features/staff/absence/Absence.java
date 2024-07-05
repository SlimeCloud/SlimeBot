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
}
