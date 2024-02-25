package de.slimecloud.slimeball.features.quote;

import de.cyklon.jevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@AllArgsConstructor
public class QuoteDeleteEvent extends CancellableEvent {
	private final Member target;
}
