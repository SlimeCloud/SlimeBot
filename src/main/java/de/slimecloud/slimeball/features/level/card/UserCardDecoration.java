package de.slimecloud.slimeball.features.level.card;

import de.mineking.javautils.database.Column;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class UserCardDecoration {
	private final SlimeBot bot;

	@Column(key = true)
	private final long guild;

	@Column(key = true)
	private final UserSnowflake user;

	@Column
	private final Set<String> decorations = new HashSet<>();

	public UserCardDecoration(@NotNull SlimeBot bot) {
		this(bot, 0, null);
	}

	@NotNull
	public static UserCardDecoration empty(@NotNull SlimeBot bot, @NotNull Member member) {
		return new UserCardDecoration(bot, member.getGuild().getIdLong(), member);
	}
}
