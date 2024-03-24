package de.slimecloud.slimeball.features.level;

import de.mineking.discordutils.ui.state.DataState;
import de.slimecloud.slimeball.features.level.card.CardProfileData;
import de.slimecloud.slimeball.features.level.card.CardRenderer;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.graphic.Graphic;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.List;

public class Leaderboard extends Graphic {
	public final static int gap = 40;
	public final static int height = 300;

	private final SlimeBot bot;
	private final DataState<?> state;
	private final List<Level> data;

	protected Leaderboard(@NotNull SlimeBot bot, @NotNull DataState<?> state, @NotNull List<Level> data) {
		super(CardRenderer.width, data.size() * height + (data.size() - 1) * gap);
		this.bot = bot;
		this.state = state;
		this.data = data;

		finish();
	}

	@Override
	protected void drawGraphic(@NotNull Graphics2D graphics) throws IOException {
		Guild guild = state.getEvent().getGuild();
		int maxLevel = data.stream().mapToInt(Level::getLevel).max().orElse(0);

		for (int i = 0; i < data.size(); i++) {
			Level level = data.get(i);
			Member member = guild.getMember(level.getUser());

			CardProfileData card = bot.getCardProfiles().getProfile(member).getData();

			int y = i * (gap + height);
			graphics.setClip(new RoundRectangle2D.Double(0, y, width, height, height / 8.0, height / 8.0));
			graphics.drawImage(card.renderPreview(member, maxLevel).getImage(), 0, y, width, height, null);
		}
	}
}
