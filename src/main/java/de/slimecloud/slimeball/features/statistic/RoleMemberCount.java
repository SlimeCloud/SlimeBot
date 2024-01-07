package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.StringUtil;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AllArgsConstructor
public class RoleMemberCount extends ListenerAdapter {
	private final SlimeBot bot;

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event) {
		update(event.getGuild());
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		update(event.getGuild());
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		update(event.getGuild());
	}

	@Override
	public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
		update(event.getGuild());
	}

	@Override
	public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
		update(event.getGuild());
	}

	public void update(@NotNull Guild guild) {
		bot.loadGuild(guild.getIdLong()).getStatistic().ifPresent(c -> update(c, guild));
	}

	public void update(@NotNull StatisticConfig config, @NotNull Guild guild) {
		config.getRoleMemberCountChannel().forEach((roleId, channel) -> {
			String format = config.getRoleMemberCountFormat().containsKey(roleId)
					? config.getRoleMemberCountFormat().get(roleId)
					: config.getDefaultRoleFormat();

			Role role = bot.getJda().getRoleById(roleId);

			bot.getJda().getVoiceChannelById(channel).getManager().setName(StringUtil.format(format, Map.of(
					"role_name", role.getName(),
					"members", guild.getMembersWithRoles(role).size()
			))).queue();
		});
	}
}
