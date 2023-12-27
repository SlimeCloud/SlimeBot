package de.slimecloud.slimeball.events;

import de.cyklon.jevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.UserSnowflake;

@Getter
@AllArgsConstructor
public class UserQuotedEvent extends CancellableEvent {
	private final UserSnowflake user;
	private final String message;
}
