package com.slimebot.message;

import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class StaffMessage extends ListenerAdapter {
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		updateMessage(event.getGuild(), event.getRoles());
	}

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		updateMessage(event.getGuild(), event.getRoles());
	}

	@Override
	public void onGuildReady(GuildReadyEvent event) {
		updateMessage(event.getGuild());
	}

	public static void updateMessage(Guild guild, List<Role> roles) {
		List<Long> staffRoles = Main.database.handle(handle -> handle.createQuery("select role from staff_roles where guild = :guild")
				.bind("guild", guild.getIdLong())
				.mapTo(Long.class)
				.list()
		);

		if(roles.stream().map(Role::getIdLong).noneMatch(staffRoles::contains)) return;

		updateMessage(guild);
	}

	public static void updateMessage(Guild guild) {
		Main.database.getChannel(guild, DatabaseField.STAFF_CHANNEL).ifPresent(channel -> {
			String content = buildMessage(guild);
			Long message = Main.database.handle(handle -> handle.createQuery("select message from staff_config where guild = :guild")
					.bind("guild", guild.getIdLong())
					.mapTo(Long.class)
					.findOne().orElse(null)
			);

			if(message == null || message == 0) {
				channel.sendMessage(content).queue(id -> Main.database.run(handle -> handle.createUpdate("update staff_config set message = :message where guild = :guild")
						.bind("message", id.getIdLong())
						.bind("guild", guild.getIdLong())
						.execute()
				));
			}

			else {
				channel.editMessageById(message, content).queue();
			}
		});
	}

	public static String buildMessage(Guild guild) {
		List<StaffRole> roles = Main.database.handle(handle -> handle.createQuery("select * from staff_roles where guild = :guild")
				.bind("guild", guild.getIdLong())
				.mapTo(StaffRole.class)
				.list()
		);

		StringBuilder builder = new StringBuilder();

		for(StaffRole role : roles) {
			if(role == null) continue;

			List<Member> members = guild.getMembersWithRoles(role.role);

			builder.append(role.role.getAsMention()).append(" *").append(role.description).append("*\n");

			if(members.isEmpty()) {
				builder.append("*Keine Mitglieder*").append("\n");
			}

			else {
				for(Member member : members) {
					builder.append("> ").append(member.getAsMention()).append("\n");
				}
			}

			builder.append("\n");
		}

		return builder.toString();
	}
}