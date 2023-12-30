package de.slimecloud.slimeball.features.level.card;

import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.config.engine.KeyType;
import de.slimecloud.slimeball.config.engine.ValidationException;
import de.slimecloud.slimeball.features.level.Level;
import de.slimecloud.slimeball.features.level.LevelTable;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.ColorUtil;
import de.slimecloud.slimeball.util.graphic.CustomFont;
import de.slimecloud.slimeball.util.graphic.Graphic;
import de.slimecloud.slimeball.util.graphic.ImageUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

@Slf4j
@Getter
@ToString
public class CardProfileData extends Graphic implements DataClass<CardProfileData> {
	public final static Font font;

	static {
		try {
			font = CustomFont.getFont("Ubuntu.ttf", Font.BOLD);
		} catch (IOException | FontFormatException e) {
			throw new RuntimeException(e);
		}
	}

	public final static Color TRANSPARENT = ColorUtil.ofCode(0);
	public final static CardProfileData DEFAULT = new CardProfileData(null);

	private final SlimeBot bot;

	@Column(autoincrement = true, key = true)
	private int id;
	@Column
	private UserSnowflake owner;

	@Setter
	@Column(name = "public")
	private boolean isPublic = false;

	@Column
	@KeyType(ConfigFieldType.COLOR)
	private Color progressbarColor = new Color(105, 227, 73, 240);
	@Column
	@KeyType(ConfigFieldType.COLOR)
	private Color progressbarBGColor = new Color(150, 150, 150, 50);
	@Column
	@KeyType(ConfigFieldType.ENUM)
	private Style progressbarStyle = Style.ROUND;

	@Column
	@KeyType(ConfigFieldType.COLOR)
	private Color progressbarBorderColor = new Color(68, 140, 41, 255);
	@Column
	@KeyType(ConfigFieldType.INTEGER)
	private int progressbarBorderWidth = 5;

	@Column
	@KeyType(ConfigFieldType.ENUM)
	private Style avatarStyle = Style.ROUND;
	@Column
	@KeyType(ConfigFieldType.COLOR)
	private Color avatarBorderColor = TRANSPARENT;
	@Column
	@KeyType(ConfigFieldType.INTEGER)
	private int avatarBorderWidth = 10;

	@Column
	@KeyType(ConfigFieldType.COLOR)
	private Color backgroundColor = new Color(30, 30, 30, 200);
	@Column
	@KeyType(ConfigFieldType.URL)
	private String backgroundImageURL = "";
	@Column
	@KeyType(ConfigFieldType.COLOR)
	private Color backgroundBorderColor = new Color(68, 140, 41, 255);
	@Column
	@KeyType(ConfigFieldType.INTEGER)
	private int backgroundBorderWidth = 10;

	@Column
	@KeyType(ConfigFieldType.COLOR)
	private Color fontColor = Color.WHITE;
	@Column
	@KeyType(ConfigFieldType.COLOR)
	private Color fontSecondaryColor = Color.GRAY;
	@Column
	@KeyType(ConfigFieldType.COLOR)
	private Color fontLevelColor = new Color(97, 180, 237);

	public CardProfileData(@NotNull SlimeBot bot, @NotNull UserSnowflake owner) {
		super(2000, 400);
		this.bot = bot;

		this.id = 0;
		this.owner = owner;
	}

	public CardProfileData(@NotNull SlimeBot bot) {
		this(bot, null);
	}

	@NotNull
	@Override
	public Table<CardProfileData> getTable() {
		return bot.getProfileData();
	}

	@NotNull
	public CardPermission getPermission(@NotNull UserSnowflake user) {
		if(owner == null) return CardPermission.READ;
		if(owner.getIdLong() == user.getIdLong()) return CardPermission.WRITE;
		if(isPublic) return CardPermission.READ;
		return CardPermission.NONE;
	}

	@NotNull
	public CardProfileData createCopy(@NotNull UserSnowflake owner) {
		//Setting the id to 0 will make JavaUtils create a new column
		this.id = 0;
		this.owner = owner;

		return this;
	}

	public CardProfileData set(@NotNull String name, @NotNull String value) {
		try {
			Field field = getClass().getDeclaredField(name);
			field.setAccessible(true);

			if (value.isEmpty()) field.set(this, field.get(DEFAULT));
			else {
				ConfigFieldType type = field.getAnnotation(KeyType.class).value();

				if (!type.getCheck().test(field.getType(), value)) throw new ValidationException(null);
				field.set(this, type.getParse().apply(field.getType(), value));
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			logger.error("Error updating '{}'", name, e);
		}

		return this;
	}

	@NotNull
	public String get(@NotNull String name) {
		try {
			Field field = getClass().getDeclaredField(name);
			field.setAccessible(true);

			Object value = field.get(this);

			if (field.isAnnotationPresent(KeyType.class)) return field.getAnnotation(KeyType.class).value().getString().apply(value);
			else return Objects.toString(value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Member member;

	@NotNull
	public CardProfileData render(@NotNull Member member) {
		this.member = member;
		finish();
		return this;
	}

	@Override
	protected void drawGraphic(@NotNull Graphics2D graphics) {
		//Get info
		Level level = bot.getLevel().getLevel(member);

		//Render
		applyBackground(graphics);
		applyAvatar(graphics, member);
		applyProgressBar(graphics, level);

		applyText(graphics, level, member);
	}

	private void applyBackground(@NotNull Graphics2D graphics) {
		//Background color
		graphics.setColor(backgroundColor);
		graphics.fillRect(0, 0, width, height);

		//Background image (if present)
		BufferedImage backgroundImage = ImageUtil.readFromUrl(backgroundImageURL);
		if (backgroundImage != null) graphics.drawImage(backgroundImage, 0, 0, width, height, null);

		//Border
		if(backgroundBorderWidth > 0) {
			graphics.setColor(backgroundBorderColor);
			graphics.setStroke(new BasicStroke(adjustBorderWith(backgroundBorderWidth)));
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
		avatar = ImageUtil.resize(avatar, avatarWidth, avatarWidth);

		//Border
		if(avatarBorderWidth > 0) {
			graphics.setColor(avatarBorderColor);
			graphics.setStroke(new BasicStroke(adjustBorderWith(avatarBorderWidth)));

			graphics.drawRoundRect(offset, offset, avatarWidth, avatarWidth, avatarStyle.getArc(avatarWidth), avatarStyle.getArc(avatarWidth));
		}

		//Image
		graphics.setClip(avatarStyle.getShape(offset, offset, avatarWidth, avatarWidth));

		graphics.drawImage(avatar, offset, offset, avatarWidth, avatarWidth, null);
		graphics.setClip(null);
	}

	private void applyProgressBar(@NotNull Graphics2D graphics, @NotNull Level level) {
		double percentage = (double) level.getXp() / LevelTable.calculateRequiredXP(level.getLevel() + 1);

		int offset = (int) (height * 0.1);
		int progressbarHeight = height / 6;
		//Offset + avatar + offset (Could be simplified to height, but it is easier to understand this way)
		int horizontalOffset = offset + (height - 2 * offset) + offset;
		int verticalOffset = height - offset - progressbarHeight;
		int maxWidth = width - offset - horizontalOffset;

		int arc = progressbarStyle.getArc(progressbarHeight);

		//Border
		if(progressbarBorderWidth > 0) {
			graphics.setColor(progressbarBorderColor);
			graphics.setStroke(new BasicStroke(adjustBorderWith(progressbarBorderWidth)));

			graphics.drawRoundRect(horizontalOffset, verticalOffset, maxWidth, progressbarHeight, arc, arc);
		}

		//Background
		graphics.setColor(progressbarBGColor);

		graphics.fillRoundRect(horizontalOffset, verticalOffset, maxWidth, progressbarHeight, arc, arc);

		//Content
		graphics.setColor(progressbarColor);

		graphics.fillRoundRect(horizontalOffset, verticalOffset, (int) (percentage * maxWidth), progressbarHeight, arc, arc);
	}

	private void applyText(@NotNull Graphics2D graphics, @NotNull Level level, @NotNull Member member) {
		int offset = (int) (height * 0.1);
		int verticalOffset = height - offset - height / 6 - offset;

		//Name
		graphics.setColor(fontColor);
		graphics.setFont(CustomFont.getFont(font, getFontSize(50)));

		graphics.drawString(member.getEffectiveName(), offset + (height - 2 * offset) + offset, verticalOffset);

		//Required XP
		graphics.setFont(CustomFont.getFont(font, getFontSize(30)));
		graphics.setColor(fontSecondaryColor);

		String required = " / " + LevelTable.calculateRequiredXP(level.getLevel() + 1) + " XP";
		int requiredWidth = graphics.getFontMetrics().stringWidth(required);

		graphics.drawString(required, width - offset - requiredWidth, verticalOffset);

		//Current XP
		graphics.setFont(CustomFont.getFont(font, getFontSize(40)));
		graphics.setColor(fontColor);

		String current = String.valueOf(level.getXp());
		int currentWidth = graphics.getFontMetrics().stringWidth(current);

		graphics.drawString(current, width - offset - requiredWidth - currentWidth, verticalOffset);


		//Level
		String levelString = String.valueOf(level.getLevel());
		String levelName = "LEVEL ";

		graphics.setColor(fontLevelColor);

		float levelHeight = getFontSize(55);
		graphics.setFont(CustomFont.getFont(font, levelHeight));
		int levelWidth = graphics.getFontMetrics().stringWidth(levelString);

		graphics.drawString(levelString, width - offset - levelWidth, offset + levelHeight);

		graphics.setFont(CustomFont.getFont(font, getFontSize(30)));
		int nameWidth = graphics.getFontMetrics().stringWidth(levelName);
		graphics.drawString(levelName, width - offset - levelWidth - nameWidth, offset + levelHeight);

		//Rank
		int rank = level.getRank() + 1;
		if (rank == 0) return;

		String rankString = "#" + rank;

		graphics.setColor(getColor(rank));
		graphics.setFont(CustomFont.getFont(font, getFontSize(50)));

		graphics.drawString(rankString, width - offset - levelWidth - nameWidth - graphics.getFontMetrics().stringWidth(rankString) - 4 * offset, offset + levelHeight);
	}

	private int adjustBorderWith(int value) {
		return (int) (0.8 * (value * width) / 1e3);
	}

	private float getFontSize(int base) {
		return (float) ((0.8 * base * width) / 1e3);
	}

	private Color getColor(int rank) {
		return switch (rank) {
			case 1 -> new Color(232, 187, 65);
			case 2 -> new Color(121, 121, 121);
			case 3 -> new Color(182, 96, 48);
			default -> fontColor;
		};
	}
}
