package de.slimecloud.slimeball.features.level.card.badge;

import de.mineking.javautils.database.Column;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

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

	@NotNull
	public static List<String> getBadges(@NotNull SlimeBot bot) {
		return bot.getConfig().getLevel()
				.map(l -> new File(l.getBadgeFolder()).list())
				.map(Arrays::asList)
				.orElse(Collections.emptyList());
	}

	@NotNull
	public static File getBadge(@NotNull SlimeBot bot, @NotNull String name) {
		return new File(bot.getConfig().getLevel().get().getBadgeFolder(), name);
	}

	@Nullable
	public static BufferedImage readBadge(@NotNull SlimeBot bot, @NotNull String badge) throws IOException {
		var role = StringUtil.isInteger(badge) ? bot.getJda().getRoleById(badge) : null;

		if(role != null) {
			if(role.getIcon() == null) return null;
			if(role.getIcon().getIcon() == null) return null;
			return ImageIO.read(role.getIcon().getIcon().download().join());
		}

		return ImageIO.read(getBadge(bot, badge));
	}
}
