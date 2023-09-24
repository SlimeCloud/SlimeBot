package com.slimebot.level;

import com.slimebot.database.DataClass;
import com.slimebot.graphic.CustomFont;
import com.slimebot.graphic.Graphic;
import com.slimebot.graphic.ImageUtil;
import com.slimebot.level.profile.*;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;


public class RankCard extends Graphic {
	public final static int rankPadding = 60;

	private final CardProfile profile;
	private final int contentWidth;
	private final int contentHeight;
	private final int contentOffsetX;
	private final int contentOffsetY;

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
		super(3850, 700);
		this.contentWidth = 3800;
		this.contentHeight = 600;
		this.contentOffsetX = 50;
		this.contentOffsetY = 20;
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

		int avatarWidth = contentHeight - 80;
		int avatarBorderFactor = av.border().width() * 2;
		int avatarBorderOffset = av.border().width();

		int bgBorderFactor = bg.border().width() * 2;
		int bgBorderOffset = bg.border().width();

		BufferedImage avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl()));
		BufferedImage avatarBorder = new BufferedImage(avatarWidth + avatarBorderFactor, avatarWidth + avatarBorderFactor, BufferedImage.TYPE_INT_ARGB);
		ImageUtil.fill(avatarBorder, av.border().color());

		avatar = ImageUtil.resize(avatar, avatarWidth, avatarWidth);
		if (av.style() == Style.ROUND) {
			avatar = ImageUtil.circle(avatar);
			avatarBorder = ImageUtil.circle(avatarBorder);
		}

		avatar = ImageUtil.merge(avatarBorder, avatar, avatarBorderOffset, avatarBorderOffset);

		//Draw background
		BufferedImage backgroundImage = null;
		try {
			if (!bg.imageURL().isBlank()) {
				var url = new URL(bg.imageURL());
				var con = (HttpURLConnection) url.openConnection();

				con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/117.0");

				con.setConnectTimeout(5000);
				con.setReadTimeout(5000);

				backgroundImage = ImageIO.read(con.getInputStream());
			}
		} catch (MalformedURLException | SocketTimeoutException ignored) {
		}   //ignored because it wil be thrown every time an invalid url is passed.

		Color borderColor = bg.border().color();
		graphics2D.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), backgroundImage == null ? bg.color().getAlpha() : 255));
		graphics2D.fillRect(0, 0, width, height);
		if (backgroundImage != null) {
			graphics2D.drawImage(backgroundImage, bgBorderOffset, bgBorderOffset, width - bgBorderFactor, height - bgBorderFactor, bg.color(), null);
		} else {
			graphics2D.setColor(bg.color());
			graphics2D.fillRect(bgBorderOffset, bgBorderOffset, width - bgBorderFactor, height - bgBorderFactor);
		}

		graphics2D.drawImage(avatar, contentOffsetX, Math.max(0, 80 - avatarBorderOffset) + contentOffsetY, null);

		int xpRequired = Level.calculateRequiredXP(level.getLevel() + 1);
		double percentage = (double) level.getXp() / xpRequired;

		int maxBarSize = contentWidth - 160 - avatarWidth;
		int barSize = (int) (maxBarSize * percentage);

		graphics2D.setColor(bar.bgColor());
		if (bar.style() == Style.ROUND)
			graphics2D.fillRoundRect(avatarWidth + 80 + contentOffsetX, contentHeight - 120 + contentOffsetY, maxBarSize, contentHeight - 500, contentHeight - 500, contentHeight - 500);
		else
			graphics2D.fillRect(avatarWidth + 80 + contentOffsetX, contentHeight - 120 + contentOffsetY, maxBarSize, contentHeight - 500);

		graphics2D.setColor(bar.border().color());
		graphics2D.setStroke(new BasicStroke(bar.border().width()));
		if (bar.style() == Style.ROUND)
			graphics2D.drawRoundRect(avatarWidth + 80 + contentOffsetX, contentHeight - 120 + contentOffsetY, maxBarSize, contentHeight - 500, contentHeight - 500, contentHeight - 500);
		else
			graphics2D.drawRect(avatarWidth + 80 + contentOffsetX, contentHeight - 120 + contentOffsetY, maxBarSize, contentHeight - 500);

		graphics2D.setColor(bar.color());
		if (bar.style() == Style.ROUND)
			graphics2D.fillRoundRect(avatarWidth + 80 + contentOffsetX, contentHeight - 120 + contentOffsetY, barSize, contentHeight - 500, contentHeight - 500, contentHeight - 500);
		else
			graphics2D.fillRect(avatarWidth + 80 + contentOffsetX, contentHeight - 120 + contentOffsetY, barSize, contentHeight - 500);

		graphics2D.setColor(Color.WHITE);
		graphics2D.setFont(CustomFont.getFont(font, 150F));
		graphics2D.drawString(user.getEffectiveName(), avatarWidth + 100 + contentOffsetX, contentHeight - 160 + contentOffsetY);

		graphics2D.setFont(CustomFont.getFont(font, 120F));

		String xp = level.getXp() + "/" + xpRequired + " XP";
		graphics2D.drawString(xp, contentWidth - graphics2D.getFontMetrics().stringWidth(xp) - 80 + contentOffsetX, contentHeight - 160 + contentOffsetY);

		graphics2D.setFont(CustomFont.getFont(font, 150F));

		String levelString = "Level " + level.getLevel();
		String rank = level.getRank().map(i -> "#" + (i + 1)).orElse("Keiner");

		int levelWidth = graphics2D.getFontMetrics().stringWidth(levelString);
		int rankWidth = graphics2D.getFontMetrics().stringWidth(rank);

		graphics2D.drawString(levelString, contentWidth - levelWidth - 80 + contentOffsetX, contentHeight - 430 + contentOffsetY);

		graphics2D.setColor(Color.decode("#222222"));
		graphics2D.fillRoundRect(contentWidth - rankWidth - levelWidth - 330 - rankPadding + contentOffsetX, contentHeight - 430 - 100 - rankPadding + contentOffsetY, rankWidth + rankPadding, 150 + rankPadding, contentHeight - 500, contentHeight - 500);

		graphics2D.setColor(Color.WHITE);
		if(level.getRank().isEmpty()) graphics2D.setFont(CustomFont.getFont(font, Font.ITALIC, 150F));
		graphics2D.drawString(rank, contentWidth - rankWidth - levelWidth - 350 + contentOffsetX, contentHeight - 430 + contentOffsetY);
	}
}
