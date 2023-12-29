package de.slimecloud.slimeball.features.report;

import de.cyklon.jevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;

@Getter
@AllArgsConstructor
public class UserReportBlockEvent extends CancellableEvent {
	private final UserSnowflake team;
	private final Member member;
	private final String reason;
}
