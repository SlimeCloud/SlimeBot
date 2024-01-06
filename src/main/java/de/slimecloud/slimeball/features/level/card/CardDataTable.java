package de.slimecloud.slimeball.features.level.card;

import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.Listable;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.state.DataState;
import de.mineking.javautils.ID;
import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CardDataTable extends Table<CardProfileData>, Listable<CardProfileData> {
	@NotNull
	default Optional<CardProfileData> getData(@Nullable ID id, @NotNull UserSnowflake owner) {
		if (id == null) return Optional.of(new CardProfileData(getManager().getData("bot"), owner));
		return selectOne(Where.equals("id", id));
	}

	@NotNull
	default List<CardProfileData> getAll(@NotNull UserSnowflake user) {
		return selectMany(Where.equals("owner", user));
	}

	/*
	Listable implementation
	 */

	@NotNull
	@Override
	default EmbedBuilder createEmbed(@NotNull DataState<MessageMenu> state, @NotNull ListContext<CardProfileData> context) {
		Filter filter = state.getState("filter", Filter.class);

		EmbedBuilder builder = new EmbedBuilder()
				.setTitle("Profile mit Filter '**" + filter.getName() + "**'")
				.setColor(getManager().<SlimeBot>getData("bot").getColor(state.event.getGuild()))
				.setTimestamp(Instant.now());

		if (context.entries().isEmpty()) builder.setDescription("*Keine Einträge*");
		else builder.setFooter("Insgesamt " + context.entries().size() + " Profile, die dem Filter entsprechen");

		return builder;
	}

	@NotNull
	@Override
	default List<CardProfileData> getEntries(@NotNull DataState<MessageMenu> state, @NotNull ListContext<CardProfileData> context) {
		return selectMany(Where.allOf(
				state.getState("filter", Filter.class).getFilter().apply(context.event().getUser()),
				Where.equals("owner", context.event().getUser()).or(Where.equals("public", true))
		));
	}
}
