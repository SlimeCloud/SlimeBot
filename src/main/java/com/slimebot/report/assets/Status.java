package com.slimebot.report.assets;

public enum Status {
	CLOSED("Geschlossed"),
	OPEN("Offen");

	public final String str;

	Status(String str) {
		this.str = str;
	}
}
