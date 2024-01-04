package de.slimecloud.slimeball.features.reminder;

import de.mineking.javautils.database.Order;
import de.mineking.javautils.database.Table;
import de.mineking.javautils.database.Where;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface ReminderTable extends Table<Reminder> {
	@NotNull
	default Optional<Reminder> getNext() {
		return selectAll(Order.ascendingBy("time").limit(1)).stream().findFirst();
	}

	default void createReminder(Guild guild, UserSnowflake user, long time, String message) {
		new Reminder(getManager().getData("bot"), 0, guild, user, time, message).update();
	}

	default List<Reminder> getByMember(Member member) {
		return selectMany(Where.allOf(
				Where.equals("user", member.getUser().getIdLong()),
				Where.equals("guild", member.getGuild().getIdLong())
		));
	}

	default List<Reminder> getByGuild(Guild guild) {
		return selectMany(Where.equals("guild", guild.getIdLong()));
	}
}
