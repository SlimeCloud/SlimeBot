package de.slimecloud.slimeball.features.level.card.badge;

import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.Listable;
import de.mineking.discordutils.list.StringEntry;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.state.DataState;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface CardBadgeTable extends Table<CardBadgeData>, Listable<StringEntry> {
	@NotNull
	default Optional<CardBadgeData> get(@NotNull IMentionable obj) {
		return selectOne(Where.allOf(
				Where.equals("guild", SlimeBot.getGuild(obj)),
				Where.equals("target", obj)
		));
	}

	@NotNull
	default CardBadgeData getOrDefault(@NotNull IMentionable obj) {
		return get(obj).orElseGet(() -> CardBadgeData.empty(getManager().getData("bot"), obj));
	}

	@NotNull
	default Collection<String> getEffectiveBadges(@NotNull Member member) {
		return Stream.concat(Stream.of(member), member.getRoles().stream())
				.map(this::get)
				.flatMap(Optional::stream)
				.flatMap(d -> d.getBadges().stream())
				.toList();
	}

	default void grant(@NotNull IMentionable target, @NotNull String name) {
		CardBadgeData badge = getOrDefault(target);

		badge.getBadges().add(name);
		upsert(badge);
	}

	default void revoke(@NotNull IMentionable target, @NotNull String name) {
		CardBadgeData badge = getOrDefault(target);

		badge.getBadges().remove(name);
		upsert(badge);
	}

	/*
	Listable implementation
	 */

	@NotNull
	@Override
	default EmbedBuilder createEmbed(@NotNull DataState<MessageMenu> state, @NotNull ListContext<StringEntry> context) {
		SlimeBot bot = getManager().getData("bot");
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(getManager().<SlimeBot>getData("bot").getColor(state.getEvent().getGuild()))
				.setTimestamp(Instant.now());

		if (state.asMap().containsKey("badge")) builder.setTitle("Besitzer des Badges **" + state.getState("badge", String.class) + "**");
		else if (state.asMap().containsKey("user")) builder.setTitle("Badges für Nutzer **" + bot.getJda().getUserById(state.getState("user", long.class)).getName() + "**");
		else if (state.asMap().containsKey("role")) builder.setTitle("Badges für Rolle **" + bot.getJda().getRoleById(state.getState("role", long.class)).getName() + "**");
		else builder.setTitle("Alle Badges");

		if (context.entries().isEmpty()) builder.setDescription("*Keine Einträge*");
		else builder.setFooter("Insgesamt " + context.entries().size() + " Einträge");

		return builder;
	}

	@NotNull
	@Override
	default List<StringEntry> getEntries(@NotNull DataState<MessageMenu> state, @NotNull ListContext<StringEntry> context) {
		SlimeBot bot = getManager().getData("bot");

		if (state.asMap().containsKey("badge")) return selectMany(Where.fieldContainsValue("badges", state.getState("badge", String.class))).stream()
				.map(d -> d.getTarget().getAsMention())
				.map(StringEntry::new)
				.toList();

		if (state.asMap().containsKey("user")) return getEffectiveBadges(context.event().getGuild().getMemberById(state.getState("user", long.class))).stream()
				.map(s -> StringUtil.isInteger(s) ? "*Icon <@&" + s + ">*" : s)
				.map(StringEntry::new)
				.toList();

		if (state.asMap().containsKey("role")) return getOrDefault(bot.getJda().getRoleById(state.getState("role", long.class))).getBadges().stream()
				.map(s -> StringUtil.isInteger(s) ? "*Icon <@&" + s + ">*" : s)
				.map(StringEntry::new)
				.toList();

		return Stream.concat(CardBadgeData.getBadges(bot).stream().map(s -> "Custom: **" + s + "**"),
				state.getEvent().getGuild().getRoles().stream()
						.map(this::get)
						.flatMap(Optional::stream)
						.flatMap(d -> d.getBadges().stream())
						.filter(StringUtil::isNumeric)
						.map(s -> "Rolle: **<@&" + s + ">**")
				)
				.map(s -> new StringEntry("- " + s))
				.toList();
	}
}
