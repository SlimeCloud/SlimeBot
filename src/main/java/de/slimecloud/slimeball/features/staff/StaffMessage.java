package de.slimecloud.slimeball.features.staff;

import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RequiredArgsConstructor
public class StaffMessage extends ListenerAdapter {
	private final SlimeBot bot;

	@Override
	public void onMessageDelete(@NotNull MessageDeleteEvent event) {
		if (!event.isFromGuild()) return;

		GuildConfig guildConfig = bot.loadGuild(event.getGuild());
		guildConfig.getTeamMessage().ifPresent(config -> {
			if (config.getMessage() == event.getMessageIdLong()) {
				config.disable(event.getGuild());
				guildConfig.setTeamMessage(null);

				guildConfig.save();
			}
		});
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
	public void onGuildReady(@NotNull GuildReadyEvent event) {
		update(event.getGuild(), event.getGuild().getRoles());
	}

	public void update(@NotNull Guild guild, @NotNull List<Role> roles) {
		bot.loadGuild(guild).getTeamMessage().ifPresent(s -> {
			for (Role role : roles) {
				if (s.getRoles().containsKey(role.getId())) {
					s.update(guild);
					return;
				}
			}
		});
	}
}
