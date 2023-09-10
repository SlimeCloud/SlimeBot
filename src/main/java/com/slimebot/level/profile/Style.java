package com.slimebot.level.profile;

import de.mineking.discord.commands.annotated.option.CustomEnumOption;

public enum Style implements CustomEnumOption {
	SQUARE,
	ROUND;

	@Override
	public String getName() {
		return name().toLowerCase();
	}

	public boolean asState() {
		return this==ROUND;
	}

	public static Style fromState(boolean state) {
		return state ? ROUND : SQUARE;
	}
}
