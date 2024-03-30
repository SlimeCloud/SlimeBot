package de.slimecloud.slimeball.features.level;

import de.mineking.discordutils.ui.state.DataState;
import de.slimecloud.slimeball.features.level.card.CardProfileData;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.graphic.Graphic;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class Leaderboard extends Graphic {
	public final static int WIDTH = 2500;
	public final static int HEIGHT = 400;

	public final static int gap = 40;

	private final SlimeBot bot;
	private final DataState<?> state;
	private final List<Level> data;

	protected Leaderboard(@NotNull SlimeBot bot, @NotNull DataState<?> state, @NotNull List<Level> data) {
		super(WIDTH, data.size() * HEIGHT + (data.size() - 1) * gap);
		this.bot = bot;
		this.state = state;
		this.data = data;

		finish();
	}

	@Override
	protected void drawGraphic(@NotNull Graphics2D graphics) {
		Guild guild = state.getEvent().getGuild();
		int maxLevel = data.stream().mapToInt(Level::getTotalXp).max().orElse(0);

		for (int i = 0; i < data.size(); i++) {
			Level level = data.get(i);
			Member member = guild.getMember(level.getUser());

			CardProfileData card = bot.getCardProfiles().getProfile(member).getData();

			int y = i * (gap + HEIGHT);
			graphics.setClip(new RoundRectangle2D.Double(0, y, width, HEIGHT, HEIGHT / 8.0, HEIGHT / 8.0));
			graphics.drawImage(card.renderPreview(member, maxLevel).getImage(), 0, y, width, HEIGHT, null);
		}
	}
}
