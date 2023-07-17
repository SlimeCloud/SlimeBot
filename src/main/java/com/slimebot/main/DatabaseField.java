package com.slimebot.main;

public enum DatabaseField {
	LOG_CHANNEL("guild_config", "logChannel"),
	GREETINGS_CHANNEL("guild_config", "greetingsChannel"),
	PUNISHMENT_CHANNEL("guild_config", "punishmentChannel"),
	STAFF_ROLE("guild_config", "staffRole"),
	CONTRIBUTOR_ROLE("guild_config", "contributorRole"),
	COLOR("guild_config", "color"),


	FDMDS_CHANNEL("fdmds", "channel"),
	FDMDS_LOG_CHANNEL("fdmds", "logChannel"),
	FDMDS_ROLE("fdmds", "role"),

	SPOTIFY_NOTIFICATION_ROLE("spotify", "notificationRole"),
	SPOTIFY_PODCAST_CHANNEL("spotify", "podcastChannel"),
	SPOTIFY_MUSIC_CHANNEL("spotify", "musicChannel"),

	STAFF_CHANNEL("staff_config", "channel");


	public final String table;
	public final String columnName;

	DatabaseField(String table, String columnName) {
		this.table = table;
		this.columnName = columnName;
	}
}
