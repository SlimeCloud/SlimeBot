package com.slimebot.report;

public enum Status {
	CLOSED("Geschlossen", "\uD83D\uDCD5"),
	OPEN("Offen", "\uD83D\uDCC2");

	public final String str;
	public final String emoji;

	Status(String str, String emoji) {
		this.str = str;
		this.emoji = emoji;
	}
}
