package de.slimecloud.slimeball.features.github;

import de.cyklon.jevent.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;

@Getter
@AllArgsConstructor
public class ContributorAcceptedEvent extends Event {
	private final UserSnowflake target;
	private final Member team;
}
