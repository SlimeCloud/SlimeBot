package de.slimecloud.slimeball.features.staff;

import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class StaffMessage extends ListenerAdapter {
	private final SlimeBot bot;

	@Override
	public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
		update(event.getGuild());
	}

	@Override
	public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
		update(event.getGuild());
	}

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event) {
		update(event.getGuild());
	}

	public void update(@NotNull Guild guild) {
		bot.loadGuild(guild).getStaff().ifPresent(s -> s.update(guild));
	}
}
