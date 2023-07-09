package com.slimebot.report.assets;

public enum Status {
	CLOSED("Geschlossen"),
	OPEN("Offen");

	public final String str;

	Status(String str) {
		this.str = str;
	}
}
