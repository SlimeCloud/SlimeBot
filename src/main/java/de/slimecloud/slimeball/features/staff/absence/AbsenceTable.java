package de.slimecloud.slimeball.features.staff.absence;

import de.mineking.databaseutils.Order;
import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.Listable;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.state.DataState;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AbsenceTable extends Table<Absence>, Listable<Absence> {
	default Absence create(@NotNull Member member, @NotNull String reason, @NotNull Date start, @Nullable Date end) {
		return insert(new Absence(getManager().getData("bot"), member.getUser(), member.getGuild(), reason, false, start, end));
	}

	@NotNull
	default List<Absence> getExpiredAbsences() {
		return selectMany(Where.lowerOrEqual("end", new Date(System.currentTimeMillis())));
	}

	@NotNull
	default List<Absence> getStartingAbsences() {
		return selectMany(Where.allOf(
				Where.lowerOrEqual("start", new Date(System.currentTimeMillis())),
				Where.equals("started", false)
		));
	}

	@NotNull
	default Optional<Absence> getAbsence(@NotNull Member member) {
		return selectOne(Where.allOf(
				Where.equals("member", member),
				Where.equals("guild", member.getGuild())
		));
	}

	@NotNull
	default List<Absence> getAbsences(@NotNull Guild guild) {
		return selectMany(Where.equals("guild", guild), Order.ascendingBy("start"));
	}

	/*
	Listable implementation
	 */

	@NotNull
	@Override
	default EmbedBuilder createEmbed(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Absence> context) {
		EmbedBuilder builder = new EmbedBuilder()
				.setTitle("Abwesenheiten auf **" + context.event().getGuild().getName() + "**")
				.setColor(getManager().<SlimeBot>getData("bot").getColor(state.getEvent().getGuild()))
				.setTimestamp(Instant.now());

		if (context.entries().isEmpty()) builder.setDescription("*Keine Einträge*");
		else builder.setFooter("Insgesamt " + context.entries().size() + " Abwesenheiten");

		return builder;
	}

	@NotNull
	@Override
	default List<Absence> getEntries(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Absence> context) {
		return getAbsences(context.event().getGuild());
	}
}
