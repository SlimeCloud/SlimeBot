package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.StringUtil;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class RoleMemberCount extends ListenerAdapter {
	private final SlimeBot bot;

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event) {
		update(event.getGuild(), event.getGuild().getRoles());
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		update(event.getGuild(), event.getMember().getRoles());
	}

	@Override
	public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
		update(event.getGuild(), event.getRoles());
	}

	@Override
	public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
		update(event.getGuild(), event.getRoles());
	}

	@Override
	public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
		GuildConfig config = bot.loadGuild(event.getGuild());
		config.getStatistic().ifPresent(c -> {
			c.getRoleMemberCountChannel().remove(event.getChannel().getIdLong());
			c.getRoleMemberCountFormat().remove(event.getChannel().getIdLong());
		});
		config.save();
	}

	@Override
	public void onRoleDelete(@NotNull RoleDeleteEvent event) {
		GuildConfig config = bot.loadGuild(event.getGuild());
		config.getStatistic().ifPresent(c -> {
			c.getRoleMemberCountChannel().values().remove(event.getRole().getIdLong());
			c.getRoleMemberCountFormat().values().remove(event.getRole().getIdLong());
		});
		config.save();
	}

	public void update(@NotNull Guild guild, @NotNull List<Role> roles) {
		bot.loadGuild(guild.getIdLong()).getStatistic().ifPresent(c -> {
			for (Role role : roles) {
				if (c.getRoleMemberCountChannel().containsValue(role.getIdLong())) {
					update(c, guild);
					return;
				}
			}
		});
	}

	public void update(@NotNull StatisticConfig config, @NotNull Guild guild) {
		config.getRoleMemberCountChannel().forEach((channel, roleId) -> {
			String format = config.getRoleMemberCountFormat().containsKey(channel)
					? config.getRoleMemberCountFormat().get(channel)
					: config.getDefaultRoleFormat();

			Role role = bot.getJda().getRoleById(roleId);

			bot.getJda().getVoiceChannelById(channel).getManager().setName(StringUtil.format(format, Map.of(
					"role_name", role.getName(),
					"members", guild.getMembersWithRoles(role).size()
			))).queue();
		});
	}
}
