package com.slimebot.level;

import com.slimebot.database.DataClass;
import com.slimebot.graphic.CustomFont;
import com.slimebot.graphic.Graphic;
import com.slimebot.graphic.ImageUtil;
import com.slimebot.level.profile.Avatar;
import com.slimebot.level.profile.Background;
import com.slimebot.level.profile.CardProfile;
import com.slimebot.level.profile.Progressbar;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static com.slimebot.level.profile.CardProfile.ROUND;

public class RankCard extends Graphic {
	public final static int rankPadding = 60;

	private final CardProfile profile;

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
		this.profile = DataClass.load(() -> new CardProfile(level.getGuild(), level.getUser()), Map.of("guild", level.getGuild(), "user", level.getUser())).orElseGet(() -> new CardProfile(level.getGuild(), level.getUser()));
		finish();
	}

	@Override
	public void drawGraphic(Graphics2D graphics2D) throws IOException {
		User user = Main.jdaInstance.getUserById(level.getUser());

		assert user != null;

		Progressbar bar = profile.getProgressBar();
		Avatar av = profile.getAvatar();
		Background bg = profile.getBackground();

		int avatarWidth = height - 80;
		int avatarBorderFactor = av.border().width()*2;
		int avatarBorderOffset = av.border().width();

		int bgBorderFactor = bg.border().width()*2;
		int bgBorderOffset = bg.border().width();

		BufferedImage avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl()));
		BufferedImage avatarBorder = new BufferedImage(avatarWidth+avatarBorderFactor, avatarWidth+avatarBorderFactor, BufferedImage.TYPE_INT_ARGB);
		ImageUtil.fill(avatarBorder, av.border().color());

		avatar = ImageUtil.resize(avatar, avatarWidth, avatarWidth);
		if (av.style()==ROUND) {
			avatar = ImageUtil.circle(avatar);
			avatarBorder = ImageUtil.circle(avatarBorder);
		}

		avatar = ImageUtil.merge(avatarBorder, avatar, avatarBorderOffset, avatarBorderOffset);

		//Draw background
		BufferedImage backgroundImage = null;
		try {
			if (!bg.imageURL().isBlank()) backgroundImage = ImageIO.read(new URL(bg.imageURL()));
		} catch (Exception ignored) {}   //ignored because it wil be thrown every time a invalid url is passed.

		Color borderColor = bg.border().color();
		graphics2D.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), bg.color().getAlpha()));
		graphics2D.fillRect(0, 0, width, height);
		if (backgroundImage!=null) {
			graphics2D.drawImage(backgroundImage, bgBorderOffset, bgBorderOffset, width-bgBorderFactor, height-bgBorderFactor, bg.color(), null);
		} else {
			graphics2D.setColor(bg.color());
			graphics2D.fillRect(bgBorderOffset, bgBorderOffset, width-bgBorderFactor, height-bgBorderFactor);
		}

		graphics2D.drawImage(avatar, 0, Math.max(0, 80-avatarBorderOffset), null);

		int xpRequired = Level.calculateRequiredXP(level.getLevel() + 1);
		double percentage = (double) level.getXp() / xpRequired;

		int maxBarSize = width - 160 - avatarWidth;
		int barSize = (int) (maxBarSize * percentage);

		graphics2D.setColor(bar.bgColor());
		graphics2D.fillRoundRect(avatarWidth + 80, height - 120, maxBarSize, height - 500, height - 500, height - 500);

		graphics2D.setColor(bar.border().color());
		graphics2D.setStroke(new BasicStroke(bar.border().width()));
		if (bar.style()==ROUND) graphics2D.drawRoundRect(avatarWidth + 80, height - 120, maxBarSize, height - 500, height - 500, height - 500);
		else graphics2D.drawRect(avatarWidth + 80, height - 120, maxBarSize, height - 500);

		graphics2D.setColor(bar.color());
		if (bar.style()==ROUND) graphics2D.fillRoundRect(avatarWidth + 80, height - 120, barSize, height - 500, height - 500, height - 500);
		else graphics2D.fillRect(avatarWidth + 80, height - 120, barSize, height - 500);

		graphics2D.setColor(Color.WHITE);
		graphics2D.setFont(CustomFont.getFont(font, 150F));
		graphics2D.drawString(user.getEffectiveName(), avatarWidth + 100, height - 160);

		graphics2D.setFont(CustomFont.getFont(font, 120F));

		String xp = level.getXp() + "/" + xpRequired + " XP";
		graphics2D.drawString(xp, width - graphics2D.getFontMetrics().stringWidth(xp) - 80, height - 160);

		graphics2D.setFont(CustomFont.getFont(font, 150F));

		String levelString = "Level " + level.getLevel();
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
