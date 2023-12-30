package de.slimecloud.slimeball.features.level.card;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardPermission {
	NONE(0),
	READ(1),
	WRITE(2);

	private final int level;

	public boolean canRead() {
		return level >= 1;
	}

	public boolean canWrite() {
		return level >= 2;
	}
}
