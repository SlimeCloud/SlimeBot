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
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BirthdayTable extends Table<Birthday>, Listable<Birthday> {

	default void remove(@NotNull Member member) {
		remove(member.getGuild().getIdLong(), member);
	}

	default void remove(long guild, @NotNull UserSnowflake user) {
		get(guild, user).ifPresent(Birthday::delete);
	}

	@NotNull
	default Birthday set(@NotNull Member member, @NotNull Instant date) {
		return set(member.getGuild().getIdLong(), member, date);
	}

	@NotNull
	default Birthday set(long guild, @NotNull UserSnowflake user, @NotNull Instant date) {
		return new Birthday(getManager().getData("bot"), guild, user, date).update();
	}

	@NotNull
	default Optional<Birthday> get(@NotNull Member member) {
		return get(member.getGuild().getIdLong(), member);
	}

	@NotNull
	default Optional<Birthday> get(long guild, @NotNull UserSnowflake user) {
		return selectOne(Where.allOf(
				Where.equals("user", user.getIdLong()),
				Where.equals("guild", guild)
		));
	}

	@NotNull
	@Override
	default List<Birthday> getEntries(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Birthday> context) {
		return selectAll().stream()
				.sorted()
				.toList();
	}

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
}
