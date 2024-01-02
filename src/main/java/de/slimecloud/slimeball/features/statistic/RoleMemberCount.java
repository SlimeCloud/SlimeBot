package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.types.AtomicString;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
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

	private int getMemberCount(@NotNull Guild guild, @NotNull Role role) {
		return (int) guild.getMembers()
				.stream()
				.filter(m -> m.getRoles().contains(role))
				.count();
	}

	@NotNull
	@SuppressWarnings("ConstantConditions")
	private String getFormat(@NotNull StatisticConfig config, long role, @NotNull Map<String, Object> values) {
		AtomicString format = new AtomicString(config.getRoleMemberCountFormat().get(role));
		if (format.isEmpty()) format.set(config.getDefaultRoleFormat());

		values.forEach((k, v) -> format.set(format.get().replace("%" + k + "%", String.valueOf(v))));

		return format.get();
	}

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event) {
		update(event);
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		update(event);
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		update(event);
	}

	@Override
	public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
		update(event);
	}

	@Override
	public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
		update(event);
	}

	@SuppressWarnings("ConstantConditions")
	private void update(@NotNull GenericGuildEvent event) {
		Guild guild = event.getGuild();

		StatisticConfig config = bot.loadGuild(guild.getIdLong()).getStatistic().orElse(null);
		if (config == null) return;

		config.getRoleMemberCountChannel().forEach((k, v) -> update(config, guild, guild.getRoleById(k), guild.getVoiceChannelById(v)));
	}

	private void update(@NotNull StatisticConfig config, @NotNull Guild guild, @NotNull Role role, @NotNull VoiceChannel channel) {
		int memberCount = getMemberCount(guild, role);
		String format = getFormat(config, role.getIdLong(), Map.of("role_name", role.getName(), "members", memberCount));

		channel.getManager().setName(format).queue();
	}
}
