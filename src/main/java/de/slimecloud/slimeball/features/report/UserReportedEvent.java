package de.slimecloud.slimeball.features.report;

import de.cyklon.jevent.CancellableEvent;
import de.slimecloud.slimeball.features.report.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

@Getter
@AllArgsConstructor
public class UserReportedEvent extends CancellableEvent {
	private final IReplyCallback event;
	private final Report report;
}
