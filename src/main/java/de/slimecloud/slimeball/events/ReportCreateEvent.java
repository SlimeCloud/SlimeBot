package de.slimecloud.slimeball.events;

import de.cyklon.jevent.Event;
import de.slimecloud.slimeball.features.report.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportCreateEvent extends Event {
	private final Report report;
}
