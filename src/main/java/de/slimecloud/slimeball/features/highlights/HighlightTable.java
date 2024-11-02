package de.slimecloud.slimeball.features.highlights;

import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.Listable;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.state.DataState;
import de.slimecloud.slimeball.features.highlights.event.HighlightRemoveEvent;
import de.slimecloud.slimeball.features.highlights.event.HighlightSetEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface HighlightTable extends Table<Highlight>, Listable<Highlight> {
	/**
	 * @return null if the member already has a highlight with this phrase
	 */
	@Nullable
	default Highlight set(@NotNull Member member, @NotNull String phrase) {
		Highlight highlight = get(member.getGuild(), phrase).orElseGet(() -> new Highlight(getManager().getData("bot"), member.getGuild(), phrase, new HashSet<>()));
		if (!highlight.getUsers().contains(member) && !new HighlightSetEvent(highlight, member).callEvent()) {
			highlight.getUsers().add(member);
			return highlight.upsert();
		}
		return null;
	}

	/**
	 * @return null if the member has no highlight with this phrase
	 */
	@Nullable
	default Highlight remove(@NotNull Member member, @NotNull String phrase) {
		Highlight highlight = get(member.getGuild(), phrase).orElse(null);
		Set<UserSnowflake> users;
		if (highlight == null || !(users = highlight.getUsers()).contains(member.getUser())) return null;
		if (users.contains(member.getUser()) && !new HighlightRemoveEvent(highlight, member).callEvent()) {
			users.remove(member.getUser());
			return upsert(new Highlight(getManager().getData("bot"), member.getGuild(), phrase, users));
		}
		return highlight;
	}

	@NotNull
	default Set<UserSnowflake> getUsers(@NotNull Guild guild, @NotNull String phrase) {
		return get(guild, phrase)
				.map(Highlight::getUsers)
				.orElseGet(HashSet::new);
	}

	@NotNull
	default Optional<Highlight> get(@NotNull Guild guild, @NotNull String phrase) {
		return selectOne(Where.allOf(
				Where.equals("guild", guild),
				Where.equals("phrase", phrase)
		));
	}

	@NotNull
	default List<Highlight> get(@NotNull Guild guild) {
		return selectMany(Where.equals("guild", guild));
	}

	@NotNull
	default List<Highlight> getHighlights(@NotNull UserSnowflake user) {
		return selectMany(Where.fieldContainsValue("users", user));
	}

	/*
	Listable implementation
	 */
	@NotNull
	@Override
	default EmbedBuilder createEmbed(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Highlight> context) {
		EmbedBuilder builder = new EmbedBuilder()
				.setTitle("Deine Highlights")
				.setColor(getManager().<SlimeBot>getData("bot").getColor(state.getEvent().getGuild()))
				.setTimestamp(Instant.now());

		if (context.entries().isEmpty()) builder.setDescription("Du hast noch keine Highlights hinzugefügt.\nMit " + getManager().<SlimeBot>getData("bot").getDiscordUtils().getCommandManager().getCommand(HighlightCommand.HighlightAddCommand.class).getAsMention() + " kannst du highlights hinzufügen");
		else builder.setFooter("Insgesamt " + context.entries().size() + " Highlights, die dem Filter entsprechen");

		return builder;
	}

	@NotNull
	@Override
	default List<Highlight> getEntries(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Highlight> context) {
		return selectMany(Where.allOf(
				Where.equals("guild", context.event().getGuild()),
				Where.fieldContainsValue("users", context.event().getUser())
		));
	}
}
