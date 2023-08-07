package com.slimebot.level.profile;

import de.mineking.discord.commands.annotated.OptionEnum;

public enum Style implements OptionEnum {
	SQUARE,
	ROUND;

	@Override
	public String getName() {
		return name().toLowerCase();
	}
}
