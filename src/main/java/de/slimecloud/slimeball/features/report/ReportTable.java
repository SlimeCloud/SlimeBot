package de.slimecloud.slimeball.features.report;

import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.Listable;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.state.DataState;
import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import de.slimecloud.slimeball.events.ReportCreateEvent;
import de.slimecloud.slimeball.events.UserReportedEvent;
import de.slimecloud.slimeball.features.report.commands.DetailsCommand;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReportTable extends Table<Report>, Listable<Report> {
	default boolean reportUser(@NotNull IReplyCallback event, @NotNull UserSnowflake target, @NotNull String reason) {
		return report(new Report(
				getManager().getData("bot"),
				Type.USER,
				event.getGuild(),
				event.getUser(),
				target,
				reason
		), event);
	}

	default boolean reportMessage(@NotNull IReplyCallback event, @NotNull Message message) {
		return report(new Report(
				getManager().getData("bot"),
				Type.MESSAGE,
				event.getGuild(),
				event.getUser(),
				message.getAuthor(),
				"[" + (message.getContentDisplay().isBlank() ? "*Kein Inhalt*" : StringUtils.abbreviate(message.getContentRaw(), 800)) + "](" + message.getJumpUrl() + ")"
		), event);
	}

	default boolean report(@NotNull Report report, @NotNull IReplyCallback event) {
		//Call event and insert save if not canceled
		if (!new UserReportedEvent(event, report).callEvent()) {
			insert(report);
			new ReportCreateEvent(report).callEvent();
			return true;
		} else return false;
	}

	@NotNull
	default Optional<Report> getReport(int id) {
		return selectOne(Where.equals("id", id));
	}

	/*
	Listable implementation
	 */

	@NotNull
	@Override
	default EmbedBuilder createEmbed(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Report> context) {
		Filter filter = Filter.valueOf(state.getState("filter"));

		EmbedBuilder builder = new EmbedBuilder()
				.setTitle("Reports mit Filter '**" + filter.getName() + "**'")
				.setColor(getManager().<SlimeBot>getData("bot").getColor(state.event.getGuild()))
				.setTimestamp(Instant.now());

		if (context.entries().isEmpty()) builder.setDescription("*Keine Einträge*");
		else builder
				.setDescription("Nutze " + context.manager().getManager().getCommandManager().getCommand(DetailsCommand.class).getAsMention() + " oder das Dropdown menu um mehr infos zu einem Report zu bekommen.\n\n **Einträge**\n")
				.setFooter("Insgesamt " + context.entries().size() + " Reports, die dem Filter entsprechen");

		return builder;
	}

	@NotNull
	@Override
	default List<Report> getEntries(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Report> context) {
		return selectMany(Where.allOf(
				Where.equals("guild", context.event().getGuild().getIdLong()),
				Filter.valueOf(state.getState("filter")).getFilter()
		));
	}
}
