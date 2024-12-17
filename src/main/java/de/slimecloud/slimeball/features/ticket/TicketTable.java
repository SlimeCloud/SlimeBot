package de.slimecloud.slimeball.features.ticket;

import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface TicketTable extends Table<Ticket> {

	default List<Ticket> getTickets(@NotNull Guild guild, @NotNull UserSnowflake user) {
		return selectMany(Where.allOf(
				Where.equals("user", user),
				Where.equals("guild", guild)
		));
	}

	default Optional<Ticket> getTicket(long id) {
		return selectOne(Where.equals("id", id));
	}

}
