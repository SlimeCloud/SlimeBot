package com.slimebot.main;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;

public class Checks {
	public static boolean hasTeamRole(Member member) {
		return Main.database.handle(handle -> handle.createQuery("select staffRole from guildConfiguration where guild = :guild")
						.bind("gulid", member.getGuild().getId())
						.mapTo(long.class)
						.findOne()
				).flatMap(id -> Optional.ofNullable(member.getGuild().getRoleById(id)))
				.map(role -> member.getRoles().contains(role))
				.orElse(member.hasPermission(Permission.MANAGE_SERVER));
	}
}
