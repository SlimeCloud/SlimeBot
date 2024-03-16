package de.slimecloud.slimeball.features.reminder;

import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.Listable;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.state.DataState;
import de.mineking.javautils.database.Order;
import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReminderTable extends Table<Reminder>, Listable<Reminder> {
	@NotNull
	default Optional<Reminder> getNext() {
		return selectAll(Order.ascendingBy("time").limit(1)).stream().findFirst();
	}

	default Reminder createReminder(@NotNull Member member, Role role, @NotNull Instant time, @NotNull Instant timeSet, @NotNull String message) {
		SlimeBot bot = getManager().getData("bot");

		Reminder result = insert(new Reminder(bot, 0, member.getGuild(), member, role, time, timeSet, message));
		bot.getRemindManager().scheduleNextReminder();
		return result;
	}

	@NotNull
	default List<Reminder> getByMember(@NotNull Member member) {
		return selectMany(Where.allOf(
				Where.equals("user", member.getUser()),
				Where.equals("guild", member.getGuild()),
				Where.equals("role", null)
		), Order.ascendingBy("time"));
	}

	@NotNull
	default List<Reminder> getByGuild(@NotNull Guild guild) {
		return selectMany(Where.equals("guild", guild));
	}

	/*
	 * Listable implementation
	 */

	@NotNull
	@Override
	default EmbedBuilder createEmbed(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Reminder> context) {
		EmbedBuilder builder = new EmbedBuilder()
				.setTitle("Reminder auf **" + state.getEvent().getGuild().getName() + "**")
				.setColor(getManager().<SlimeBot>getData("bot").getColor(state.getEvent().getGuild()));

		if (context.entries().isEmpty()) builder.setDescription("*Du hast keine aktiven Reminder auf diesem Server*");
		else builder.setFooter("Insgesamt " + context.entries().size() + " Reminder");

		return builder;
	}

	@Override
	default void finalizeEmbed(@NotNull EmbedBuilder builder, @NotNull DataState<MessageMenu> state, @NotNull ListContext<Reminder> context) {
		if(context.entries().isEmpty()) return;
		builder.appendDescription("\n\nLösche einen Reminder mit " + context.manager().getManager().getCommandManager().getCommand(RemindCommand.DeleteCommand.class).getAsMention());
	}

	@NotNull
	@Override
	default List<Reminder> getEntries(@NotNull DataState<MessageMenu> state, @NotNull ListContext<Reminder> context) {
		return getByMember(context.event().getMember());
	}
}
