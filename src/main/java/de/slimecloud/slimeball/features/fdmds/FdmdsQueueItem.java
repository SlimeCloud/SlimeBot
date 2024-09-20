package de.slimecloud.slimeball.features.fdmds;

import de.mineking.databaseutils.Column;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;

import java.time.Instant;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class FdmdsQueueItem {
	private final SlimeBot bot;

	@Column(key = true)
	private long message;
	@Column
	private Guild guild;

	@Column
	private String title;

	@Column
	private Instant timestamp;
}
