package de.slimecloud.slimeball.features.fdmds;

import de.cyklon.jevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;

@Getter
@AllArgsConstructor
public class FdmdsCreateEvent extends CancellableEvent {
	private final UserSnowflake user;
	private final Guild guild;
	private final String message;
}
