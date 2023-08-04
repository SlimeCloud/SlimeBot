package com.slimebot.message;

import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.StaffConfig;
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
		GuildConfig.getConfig(guild).getStaffConfig().ifPresent(config -> {
			if (roles.stream().map(Role::getId).noneMatch(config.roles::containsKey)) return;

			updateMessage(guild);
		});
	}

	public static void updateMessage(Guild guild) {
		var config = GuildConfig.getConfig(guild);
		config.getStaffConfig().ifPresent(staff -> {
			String content = buildMessage(staff, guild);

			if (content.isEmpty()) {
				return;
			}

			if (staff.message == null || staff.message == 0) {
				staff.getChannel().ifPresent(channel -> channel.sendMessage(content).queue(mes -> {
					staff.message = mes.getIdLong();
					config.save();
				}));
			} else {
				staff.getChannel().ifPresent(channel -> channel.editMessageById(staff.message, content).queue());
			}
		});
	}

	public static String buildMessage(StaffConfig config, Guild guild) {
		StringBuilder builder = new StringBuilder();

		config.roles.forEach((roleId, description) -> {
			Role role;

			try {
				role = guild.getRoleById(roleId);
			} catch (NumberFormatException e) {
				builder.append(description).append("\n\n");
				return;
			}

			if (role == null) return;

			List<Member> members = guild.getMembersWithRoles(role);

			builder.append(role.getAsMention()).append(" **").append(description).append("**\n");

			if (members.isEmpty()) {
				builder.append("*Keine Mitglieder*").append("\n");
			} else {
				for (Member member : members) {
					builder.append("> ").append(member.getAsMention()).append("\n");
				}
			}

			builder.append("\n");
		});

		return builder.toString();
	}
}