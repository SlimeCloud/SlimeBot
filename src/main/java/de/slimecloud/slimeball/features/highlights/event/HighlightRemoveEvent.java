package de.slimecloud.slimeball.features.highlights.event;

import de.slimecloud.slimeball.features.highlights.Highlight;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

@Getter
public class HighlightRemoveEvent extends HighlightEvent {

	private final Member member;

	public HighlightRemoveEvent(@NotNull Highlight highlight, @NotNull Member member) {
		super(highlight);
		this.member = member;
	}
}
