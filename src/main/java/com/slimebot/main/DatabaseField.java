package com.slimebot.main;

public enum DatabaseField {
	LOG_CHANNEL("guildConfigration", "logChannel"),
	GREETINGS_CHANNEL("guildConfigration", "greetingsChannel"),
	PUNISHMENT_CHANNEL("guildConfigration", "punishmentChannel"),
	STAFF_ROLE("guildConfigration", "staffRole"),


	FDMDS_CHANNEL("fdmds", "channel"),
	FDMDS_LOG_CHANNEL("fdmds", "logChannel"),
	FDMDS_ROLE("fdmds", "role"),

	SPOTIFY_NOTIFICATION_ROLE("spotify", "notificationRole"),
	SPOTIFY_PODCAST_CHANNEL("spotify", "podcastChannel"),
	SPOTIFY_MUSIC_CHANNEL("spotify", "musicChannel");


	public final String table;
	public final String columnName;

	DatabaseField(String table, String columnName) {
		this.table = table;
		this.columnName = columnName;
	}
}
