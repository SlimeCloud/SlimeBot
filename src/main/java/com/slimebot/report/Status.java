package com.slimebot.report;

public enum Status {
	CLOSED("Geschlossen"),
	OPEN("Offen");

	public final String str;

	Status(String str) {
		this.str = str;
	}
}
