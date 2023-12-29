package de.slimecloud.slimeball.features.moderation;

import de.cyklon.jevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;


@Getter
@AllArgsConstructor
public class UserTimeoutedEvent extends CancellableEvent {
	private final Member target;
	private final User team;
	private final String reason;

	private final Instant end;
}
