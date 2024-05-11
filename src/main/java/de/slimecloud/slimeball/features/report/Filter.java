package de.slimecloud.slimeball.features.report;

import de.mineking.databaseutils.Where;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Filter {
	ALL(Where.empty(), "Alle"),
	CLOSED(Where.equals("status", Status.CLOSED), "Geschlossen"),
	OPEN(Where.equals("status", Status.OPEN), "Geöffnet");

	private final Where filter;
	private final String name;
}
