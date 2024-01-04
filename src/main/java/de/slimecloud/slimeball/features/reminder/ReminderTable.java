package de.slimecloud.slimeball.features.reminder;

import de.mineking.javautils.database.Order;
import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReminderTable extends Table<Reminder> {
	@NotNull
	default Optional<Reminder> getNext() {
		return selectAll(Order.ascendingBy("time").limit(1)).stream().findFirst();
	}

	default Reminder createReminder(@NotNull Member member, Role role, @NotNull Instant time, @NotNull String message) {
		SlimeBot bot = getManager().getData("bot");

		Reminder result = insert(new Reminder(bot, 0, member.getGuild(), member, role, time, message));
		bot.getRemindManager().scheduleNextReminder();
		return result;
	}

	@NotNull
	default List<Reminder> getByMember(@NotNull Member member) {
		return selectMany(Where.allOf(
				Where.equals("user", member.getUser().getIdLong()),
				Where.equals("guild", member.getGuild().getIdLong()),
				Where.equals("role", 0)
		), Order.ascendingBy("time"));
	}

	@NotNull
	default List<Reminder> getByGuild(@NotNull Guild guild) {
		return selectMany(Where.equals("guild", guild.getIdLong()));
	}
}
