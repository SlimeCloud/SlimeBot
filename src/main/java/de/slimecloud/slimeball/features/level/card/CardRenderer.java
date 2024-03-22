package de.slimecloud.slimeball.features.level.card;

import de.slimecloud.slimeball.features.level.Level;
import de.slimecloud.slimeball.features.level.LevelTable;
import de.slimecloud.slimeball.features.level.card.badge.CardBadgeData;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.graphic.CustomFont;
import de.slimecloud.slimeball.util.graphic.Graphic;
import de.slimecloud.slimeball.util.graphic.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

@Slf4j
public class CardRenderer extends Graphic {
	public final static int width = 2000;
	public final static Font font;

	static {
		try {
			font = CustomFont.getFont("DejaVuSans.ttf", Font.BOLD);
		} catch (IOException | FontFormatException e) {
			throw new RuntimeException(e);
		}
	}

	private final SlimeBot bot;
	private final CardProfileData data;

	private final Member member;
	private final Integer maxLevel;

	public CardRenderer(@NotNull SlimeBot bot, @NotNull CardProfileData data, @NotNull Member member, Integer maxLevel) {
		super(width, maxLevel == null ? 400 : 200);

		this.bot = bot;
		this.data = data;
		this.member = member;
		this.maxLevel = maxLevel;

		finish();
	}

	@Override
	protected void drawGraphic(@NotNull Graphics2D graphics) {
		//Get info
		Level level = bot.getLevel().getLevel(member);

		//Render
		applyBackground(graphics);
		applyAvatar(graphics, member);
		applyProgressBar(graphics, maxLevel == null ? (double) level.getXp() / LevelTable.calculateRequiredXP(level.getLevel() + 1) : (double) level.getLevel() / maxLevel);

		applyText(graphics, level, member);
		if(maxLevel == null) applyBadges(graphics, member);
	}

	private void applyBackground(@NotNull Graphics2D graphics) {
		//Background color
		graphics.setColor(data.getBackgroundColor());
		graphics.fillRect(0, 0, width, height);

		//Background image (if present)
		BufferedImage backgroundImage = ImageUtil.readFromUrl(data.getBackgroundImageURL());
		if (backgroundImage != null) graphics.drawImage(backgroundImage, 0, 0, width, height, null);

		//Border
		if (data.getBackgroundBorderWidth() > 0) {
			graphics.setColor(data.getBackgroundBorderColor());
			graphics.setStroke(new BasicStroke(adjustBorderWith(data.getBackgroundBorderWidth())));
			graphics.drawRoundRect(0, 0, width, height, height / 8, height / 8);
		}
	}

	private void applyAvatar(@NotNull Graphics2D graphics, @NotNull Member member) {
		//Retrieve avatar url. In theory, this should not make a request because of caching, but you never know...
		String avatarUrl = member.getEffectiveAvatarUrl();

		//Read avatar
		BufferedImage avatar = ImageUtil.readFromUrl(avatarUrl);
		if (avatar == null) return;

		//Size
		int offset = (int) (height * 0.1);
		int avatarWidth = height - 2 * offset;
		int avatarHeigt = avatarWidth;
		avatar = ImageUtil.resize(avatar, avatarWidth, avatarHeigt);

		//Border
		if (data.getAvatarBorderWidth() > 0) {
			graphics.setColor(data.getAvatarBorderColor());
			graphics.setStroke(new BasicStroke(adjustBorderWith(data.getAvatarBorderWidth())));

			graphics.drawRoundRect(offset, offset, avatarWidth, avatarHeigt, data.getAvatarStyle().getArc(avatarWidth), data.getAvatarStyle().getArc(avatarWidth));
		}

		//Image
		graphics.setClip(data.getAvatarStyle().getShape(offset, offset, avatarWidth, avatarHeigt));

		graphics.drawImage(avatar, offset, offset, avatarWidth, avatarHeigt, null);
		graphics.setClip(null);
	}

	private void applyProgressBar(@NotNull Graphics2D graphics, double percentage) {
		int offset = (int) (height * 0.1);
		int progressbarHeight = (int) Math.pow(height, 0.25) * 17;
		//Offset + avatar + offset (Could be simplified to height, but it is easier to understand this way)
		int horizontalOffset = offset + (height - 2 * offset) + offset;
		int verticalOffset = height - offset - progressbarHeight;
		int maxWidth = width - offset - horizontalOffset;

		int arc = data.getProgressbarStyle().getArc(progressbarHeight);

		//Border
		if (data.getProgressbarBorderWidth() > 0) {
			graphics.setColor(data.getProgressbarBorderColor());
			graphics.setStroke(new BasicStroke(adjustBorderWith(data.getProgressbarBorderWidth())));

			graphics.drawRoundRect(horizontalOffset, verticalOffset, maxWidth, progressbarHeight, arc, arc);
		}

		//Background
		graphics.setColor(data.getProgressbarBGColor());
		graphics.fillRoundRect(horizontalOffset, verticalOffset, maxWidth, progressbarHeight, arc, arc);

		//Content
		graphics.setColor(data.getProgressbarColor());
		graphics.fillRoundRect(horizontalOffset, verticalOffset, (int) (percentage * maxWidth), progressbarHeight, arc, arc);
	}

	private void applyText(@NotNull Graphics2D graphics, @NotNull Level level, @NotNull Member member) {
		int offset = (int) (height * 0.1);
		int verticalOffset = height - offset - (int) Math.pow(height, 0.25) * 17 - offset;

		//Name
		float nameSize = getFontSize((int) (Math.sqrt(height) * 2.5));
		graphics.setColor(data.getFontColor());
		graphics.setFont(CustomFont.getFont(font, nameSize));

		graphics.drawString(member.getEffectiveName(), offset + (height - 2 * offset) + offset, verticalOffset);

		//Level
		String levelString = String.valueOf(level.getLevel());
		String levelName = "LEVEL ";

		graphics.setColor(data.getFontLevelColor());

		graphics.setFont(CustomFont.getFont(font, nameSize));
		int levelWidth = graphics.getFontMetrics().stringWidth(levelString);

		graphics.drawString(levelString, width - offset- levelWidth, offset + nameSize);

		graphics.setFont(CustomFont.getFont(font, nameSize * 0.8F));
		int levelNameWidth = graphics.getFontMetrics().stringWidth(levelName);
		graphics.drawString(levelName, width - offset - levelWidth - levelNameWidth, offset + nameSize);

		//Rank
		int rank = level.getRank() + 1;
		if (rank == 0) return;

		String rankString = "#" + rank;
		String rankName = "RANK ";

		graphics.setColor(getColor(rank));

		graphics.setFont(CustomFont.getFont(font, nameSize));
		int rankWidth = graphics.getFontMetrics().stringWidth(rankString);

		graphics.drawString(rankString, width - offset - rankWidth - width / 25 - levelWidth - levelNameWidth, offset + nameSize);

		graphics.setFont(CustomFont.getFont(font, getFontSize(30)));
		int rankNameWidth = graphics.getFontMetrics().stringWidth(levelName);
		graphics.drawString(rankName, width - offset - rankWidth - rankNameWidth - width / 25 - levelWidth - levelNameWidth, offset + nameSize);

		if(maxLevel != null) return;

		//Required XP
		graphics.setFont(CustomFont.getFont(font, getFontSize(30)));
		graphics.setColor(data.getFontSecondaryColor());

		String required = " / " + LevelTable.calculateRequiredXP(level.getLevel() + 1) + " XP";
		int requiredWidth = graphics.getFontMetrics().stringWidth(required);

		graphics.drawString(required, width - offset - requiredWidth, verticalOffset);

		//Current XP
		graphics.setFont(CustomFont.getFont(font, getFontSize(40)));
		graphics.setColor(data.getFontColor());

		String current = String.valueOf(level.getXp());
		int currentWidth = graphics.getFontMetrics().stringWidth(current);

		graphics.drawString(current, width - offset - requiredWidth - currentWidth, verticalOffset);
	}

	private void applyBadges(@NotNull Graphics2D graphics, @NotNull Member member) {
		Collection<String> badges = bot.getCardBadges().getEffectiveBadges(member);

		int offset = (int) (height * 0.1);
		int height = (int) getFontSize(50);

		//Offset + avatar + offset (Could be simplified to height, but it is easier to understand this way)
		int x = offset + (this.height - 2 * offset) + offset;

		graphics.setColor(data.getBadgeBorderColor());
		graphics.setStroke(new BasicStroke(adjustBorderWith(data.getBadgeBorderWidth())));

		for (String d : badges) {
			try {
				BufferedImage img = CardBadgeData.readBadge(bot, d);
				if (img == null) continue;

				int width = (int) (img.getWidth() * ((double) height / img.getHeight()));

				graphics.setClip(null);
				graphics.drawRoundRect(x, offset, width, height, data.getBadgeStyle().getArc(height), data.getBadgeStyle().getArc(height));

				graphics.setClip(data.getBadgeStyle().getShape(x, offset, width, height));
				graphics.drawImage(img, x, offset, width, height, null);

				x += width + height / 2;
			} catch (FileNotFoundException e) {
				logger.warn("Badge {} not found for member {}", d, member);
			} catch (IOException e) {
				logger.error("Failed to read badge {} for member {}", d, member, e);
			}
		}
	}

	public static int adjustBorderWith(int value) {
		return (int) (0.8 * (value * width) / 1e3);
	}

	public static float getFontSize(int base) {
		return (float) ((0.8 * base * width) / 1e3);
	}

	private Color getColor(int rank) {
		return switch (rank) {
			case 1 -> new Color(232, 187, 65);
			case 2 -> new Color(192,192,192);
			case 3 -> new Color(205, 115, 50);
			default -> data.getFontColor();
		};
	}
}
