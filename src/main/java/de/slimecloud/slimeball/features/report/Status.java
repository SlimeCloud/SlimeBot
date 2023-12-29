package de.slimecloud.slimeball.features.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
	CLOSED("Geschlossen", "\uD83D\uDCD5"),
	OPEN("Offen", "\uD83D\uDCC2");

	private final String str;
	private final String emoji;
}
