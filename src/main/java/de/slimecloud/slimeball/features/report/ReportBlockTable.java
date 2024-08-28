package de.slimecloud.slimeball.features.report;

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
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReportBlockTable extends Table<ReportBlock>, Listable<ReportBlock> {
	@NotNull
	@Override
	default List<ReportBlock> getEntries(@NotNull DataState<MessageMenu> state, @NotNull ListContext<ReportBlock> context) {
		return selectMany(Where.equals("guild", context.event().getGuild()));
	}

	default boolean blockUser(@NotNull UserSnowflake team, @NotNull Member member, @NotNull String reason) {
		//Call event and insert save if not canceled
		if (!new UserReportBlockEvent(team, member, reason).callEvent()) {
			insert(new ReportBlock(member, member.getGuild(), reason));
			return true;
		} else return false;
	}

	default void unblock(@NotNull Member member) {
		delete(Where.allOf(
				Where.equals("user", member),
				Where.equals("guild", member.getGuild())
		));
	}

	@NotNull
	default Optional<ReportBlock> isBlocked(@NotNull Guild guild, @NotNull UserSnowflake user) {
		return selectOne(Where.allOf(
				Where.equals("user", user),
				Where.equals("guild", guild)
		));
	}

	@NotNull
	default Optional<ReportBlock> isBlocked(@NotNull Member member) {
		return isBlocked(member.getGuild(), member);
	}

	/*
	Listable implementation
	 */

	@NotNull
	@Override
	default EmbedBuilder createEmbed(@NotNull DataState<MessageMenu> state, @NotNull ListContext<ReportBlock> context) {
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(getManager().<SlimeBot>getData("bot").getColor(state.getEvent().getGuild()))
				.setTimestamp(Instant.now())
				.setTitle("Vom Report-System ausgeschlossene Nutzer");

		if (context.entries().isEmpty()) {
			builder.setDescription("*Keine Einträge*");
		} else {
			builder.setFooter("Insgesamt " + context.entries().size() + " blockierte Nutzer");
		}

		return builder;
	}
}
