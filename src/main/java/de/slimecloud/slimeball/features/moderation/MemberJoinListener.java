package de.slimecloud.slimeball.features.moderation;

import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class MemberJoinListener extends ListenerAdapter {
	private final SlimeBot bot;

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		event.getGuild().modifyMemberRoles(
				event.getMember(),
				bot.loadGuild(event.getGuild()).getJoinRoles().stream()
						.map(event.getGuild()::getRoleById)
						.toList(),
				null
		).queue();
	}
}
