package de.slimecloud.slimeball.features.highlights.event;

import de.slimecloud.slimeball.features.highlights.Highlight;
import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
public class HighlightTriggeredEvent extends HighlightEvent {

	private final Message message;

	public HighlightTriggeredEvent(@NotNull Highlight highlight, @NotNull Message message) {
		super(highlight);
		this.message = message;
	}
}
