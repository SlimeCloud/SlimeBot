package de.slimecloud.slimeball.features.fdmds;

import de.mineking.databaseutils.Order;
import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.Where;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

public interface FdmdsQueue extends Table<FdmdsQueueItem> {
	default boolean removeItemFromQueue(long messageId) {
		return delete(Where.equals("message", messageId)) > 0;
	}

	default void addItemToQueue(@NotNull Message message) {
		upsert(new FdmdsQueueItem(getManager().getData("bot"), message.getIdLong(), message.getGuild(), message.getEmbeds().get(0).getTitle(), Instant.now()));
	}

	@NotNull
	default List<FdmdsQueueItem> getNextItems(@NotNull Guild guild, int limit) {
		return selectMany(Where.equals("guild", guild), Order.ascendingBy("timestamp"));
	}
}
