package de.slimecloud.slimeball.features.level;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.Listener;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Listener
public class LevelUpListener {

	@EventHandler
	public void levelUpMessage(@NotNull SlimeBot bot, @NotNull UserLevelUpEvent event) {
		bot.loadGuild(event.getUser().getGuild()).getLevel().flatMap(GuildLevelConfig::getChannel).ifPresent(channel -> channel
				.sendMessage(
						bot.getConfig().getLevel().get().getLevelUpMessage()
								.replace("%user%", event.getUser().getAsMention())
								.replace("%level%", String.valueOf(event.getNewLevel()))
				).queue()
		);
	}

	@EventHandler
	public void levelRoles(@NotNull SlimeBot bot, @NotNull UserLevelUpEvent event) {
		updateLevelRoles(bot, event.getUser(), event.getNewLevel());
	}

	public static void updateLevelRoles(@NotNull SlimeBot bot, @NotNull Member member, int level) {
		bot.loadGuild(member.getGuild()).getLevel().map(GuildLevelConfig::getLevelRoles).ifPresent(roles -> {
			//Find highest role
			Optional<Long> levelRoleId = getLevelRole(bot, member.getGuild(), level);

			member.getGuild().modifyMemberRoles(
					member,
					levelRoleId.map(member.getGuild()::getRoleById).stream().toList(),
					roles.values().stream()
							.filter(r -> levelRoleId.isEmpty() || !Objects.equals(r, levelRoleId.get()))
							.map(member.getGuild()::getRoleById)
							.toList()
			).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));
		});
	}

	@NotNull
	public static Optional<Long> getLevelRole(@NotNull SlimeBot bot, @NotNull Guild guild, int level) {
		return bot.loadGuild(guild).getLevel()
				.map(GuildLevelConfig::getLevelRoles)
				.flatMap(roles -> roles.entrySet().stream()
						.filter(e -> level >= e.getKey())
						.max(Comparator.comparingInt(Map.Entry::getKey))
						.map(Map.Entry::getValue)
				);
	}
}
