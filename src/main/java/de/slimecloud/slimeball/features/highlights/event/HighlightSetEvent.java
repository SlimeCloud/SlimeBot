package de.slimecloud.slimeball.features.highlights.event;

import de.slimecloud.slimeball.features.highlights.Highlight;
import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
public class HighlightSetEvent extends HighlightEvent {

	private final Member member;

	public HighlightSetEvent(@NotNull Highlight highlight, @NotNull Member member) {
		super(highlight);
		this.member = member;
	}
}
