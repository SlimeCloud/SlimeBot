package de.slimecloud.slimeball.features.fdmds;

import de.cyklon.jevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@AllArgsConstructor
public class FdmdsSubmitedEvent extends CancellableEvent {
	private final Member user;
	private final String question;
}
