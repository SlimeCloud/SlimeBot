package de.slimecloud.slimeball.features.highlights.event;

import de.cyklon.jevent.CancellableEvent;
import de.slimecloud.slimeball.features.highlights.Highlight;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class HighlightEvent extends CancellableEvent {

	private final Highlight highlight;
	private final Guild guild;
	private final String phrase;

	protected HighlightEvent(@NotNull Highlight highlight) {
		this(highlight, highlight.getGuild(), highlight.getPhrase());
	}
}
