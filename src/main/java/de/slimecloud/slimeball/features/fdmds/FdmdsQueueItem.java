package de.slimecloud.slimeball.features.fdmds;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class FdmdsQueueItem implements DataClass<FdmdsQueueItem> {
	private final SlimeBot bot;

	@Column(key = true)
	private long message;
	@Column
	private Guild guild;

	@Column
	private Instant timestamp;

	@NotNull
	@Override
	public Table<FdmdsQueueItem> getTable() {
		return bot.getFdmdsQueue();
	}
}
