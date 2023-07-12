package com.slimebot.main;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;

public class Checks {
	public static boolean hasTeamRole(Member member) {
		return Optional.ofNullable(Main.database.getRole(member.getGuild(), DatabaseField.STAFF_ROLE))
				.map(role -> !member.getRoles().contains(role))
				.orElse(!member.hasPermission(Permission.MANAGE_SERVER));
	}
}
