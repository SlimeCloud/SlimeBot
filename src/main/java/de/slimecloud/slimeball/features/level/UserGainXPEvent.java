package de.slimecloud.slimeball.features.level;

import de.cyklon.jevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@AllArgsConstructor
public class UserGainXPEvent extends CancellableEvent {
	public enum Type {
		MESSAGE,
		VOICE,
		MANUAL
	}

	private final Member user;
	private final Type type;

	private final int level;
	private final int oldXp;
	private final int newXp;
}
