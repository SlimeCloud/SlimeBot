package com.slimebot.level;

import com.slimebot.graphic.CustomFont;
import com.slimebot.graphic.Graphic;
import com.slimebot.graphic.ImageUtil;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class RankCard extends Graphic {
	public final static int rankPadding = 60;

	public final static Color barBackground = new Color(150, 150, 150, 50);
	public final static Color barOutline = new Color(68, 140, 41, 255);
	public final static Color barForeground = new Color(105, 227, 73, 200);

	private final static Font font;

	static {
		try {
			font = CustomFont.getFont("Ubuntu.ttf", Font.BOLD, 80);
		} catch (IOException | FontFormatException e) {
			throw new RuntimeException(e);
		}
	}

	private final Level level;

	public RankCard(Level level) {
		super(3800, 600);
		this.level = level;
		finish();
	}

	@Override
	public void drawGraphic(Graphics2D graphics2D) throws IOException {
		User user = Main.jdaInstance.getUserById(level.user());

		int avatarWidth = height - 80;

		BufferedImage avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl()));

		avatar = ImageUtil.resize(avatar, avatarWidth, avatarWidth);
		avatar = ImageUtil.circle(avatar);

		graphics2D.drawImage(avatar, 0, 80, null);

		int xpRequired = Level.calculateRequiredXP(level.level() + 1);
		double percentage = (double) level.xp() / xpRequired;

		int maxBarSize = width - 160 - avatarWidth;
		int barSize = (int) (maxBarSize * percentage);

		graphics2D.setColor(barBackground);
		graphics2D.fillRoundRect(avatarWidth + 80, height - 120, maxBarSize, height - 500, height - 500, height - 500);

		graphics2D.setColor(barOutline);
		graphics2D.setStroke(new BasicStroke(15));
		graphics2D.drawRoundRect(avatarWidth + 80, height - 120, maxBarSize, height - 500, height - 500, height - 500);

		graphics2D.setColor(barForeground);
		graphics2D.fillRoundRect(avatarWidth + 80, height - 120, barSize, height - 500, height - 500, height - 500);

		graphics2D.setColor(Color.WHITE);
		graphics2D.setFont(CustomFont.getFont(font, 150F));
		graphics2D.drawString(user.getEffectiveName(), avatarWidth + 100, height - 160);

		graphics2D.setFont(CustomFont.getFont(font, 120F));

		String xp = level.xp() + "/" + xpRequired + " XP";
		graphics2D.drawString(xp, width - graphics2D.getFontMetrics().stringWidth(xp) - 80, height - 160);

		graphics2D.setFont(CustomFont.getFont(font, 150F));

		String levelString = "Level " + level.level();
		String rank = "#" + level.getRank().map(i -> String.valueOf(i + 1)).orElse("Keiner");

		int levelWidth = graphics2D.getFontMetrics().stringWidth(levelString);
		int rankWidth = graphics2D.getFontMetrics().stringWidth(rank);

		graphics2D.drawString(levelString, width - levelWidth - 80, height - 430);

		graphics2D.setColor(Color.decode("#222222"));
		graphics2D.fillRoundRect(width - rankWidth - levelWidth - 330 - rankPadding, height - 430 - 100 - rankPadding, rankWidth + rankPadding, 150 + rankPadding, height - 500, height - 500);

		graphics2D.setColor(Color.WHITE);
		graphics2D.drawString(rank, width - graphics2D.getFontMetrics().stringWidth(rank) - graphics2D.getFontMetrics().stringWidth(levelString) - 350, height - 430);
	}
}
