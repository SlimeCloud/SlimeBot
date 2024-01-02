package de.slimecloud.slimeball.features.birthday;

import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.Listable;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.state.DataState;
import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BirthdayTable extends Table<Birthday>, Listable<Birthday> {
	default void delete(@NotNull Member member) {
		delete(Where.allOf(
				Where.equals("guild", member.getGuild().getIdLong()),
				Where.equals("user", member.getIdLong())
		));
	}

	@NotNull
	default Birthday save(@NotNull Member member, @NotNull Instant date) {
		return insert(new Birthday(getManager().getData("bot"), member.getGuild(), member, date));
	}

	@NotNull
	default Optional<Birthday> get(@NotNull Member user) {
		return selectOne(Where.allOf(
				Where.equals("guild", user.getGuild().getIdLong()),
				Where.equals("user", user.getIdLong())
		));
	}

	/*
	Listable implementation
	 */
	@Override
	default int entriesPerPage() {
		return 10;
	}

	@NotNull
	@Override
	default EmbedBuilder createEmbed(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Birthday> context) {
		EmbedBuilder builder = new EmbedBuilder()
				.setTitle("Geburtstage")
				.setColor(getManager().<SlimeBot>getData("bot").getColor(state.event.getGuild()))
				.setTimestamp(Instant.now());

		if (context.entries().isEmpty()) builder.setDescription("*Keine Einträge*");
		else builder.setFooter("Insgesamt " + context.entries().size() + " eingetragene Geburtstage");
		return builder;
	}

	@NotNull
	@Override
	default List<Birthday> getEntries(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Birthday> context) {
		return selectAll().stream()
				.sorted()
				.toList();
	}
}
