package de.slimecloud.slimeball.features.level.card.badge;

import de.mineking.javautils.database.Column;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class CardBadgeData {
	private final SlimeBot bot;

	@Column(key = true)
	private final Guild guild;

	@Column(key = true)
	private final IMentionable target;

	@Column
	private final Set<String> badges = new HashSet<>();

	public CardBadgeData(@NotNull SlimeBot bot) {
		this(bot, null, null);
	}

	@NotNull
	public static CardBadgeData empty(@NotNull SlimeBot bot, @NotNull IMentionable target) {
		return new CardBadgeData(bot, SlimeBot.getGuild(target), target);
	}
}
