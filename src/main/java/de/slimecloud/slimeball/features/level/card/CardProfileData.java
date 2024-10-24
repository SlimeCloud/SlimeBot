package de.slimecloud.slimeball.features.level.card;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.mineking.javautils.ID;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.config.engine.Info;
import de.slimecloud.slimeball.config.engine.ValidationException;
import de.slimecloud.slimeball.features.level.card.component.RankColor;
import de.slimecloud.slimeball.features.level.card.component.Style;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.ColorUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Objects;

@Slf4j
@Getter
@ToString
public class CardProfileData implements DataClass<CardProfileData>, ListEntry {
	public final static Color TRANSPARENT = ColorUtil.ofCode(0);
	public final static CardProfileData DEFAULT = new CardProfileData(null);

	private final SlimeBot bot;

	@Column(key = true)
	private ID id;
	@Column
	private UserSnowflake owner;

	@Setter
	@Column(name = "public")
	private boolean isPublic = false;

	@Column
	@Info(keyType = ConfigFieldType.COLOR)
	private Color progressbarColor = new Color(105, 227, 73, 240);
	@Column
	@Info(keyType = ConfigFieldType.COLOR)
	private Color progressbarBGColor = new Color(150, 150, 150, 50);
	@Column
	@Info(keyType = ConfigFieldType.ENUM)
	private Style progressbarStyle = Style.ROUND_SQUARE;

	@Column
	@Info(keyType = ConfigFieldType.COLOR)
	private Color progressbarBorderColor = new Color(68, 140, 41, 255);
	@Column
	@Info(keyType = ConfigFieldType.INTEGER)
	private int progressbarBorderWidth = 5;

	@Column
	@Info(keyType = ConfigFieldType.ENUM)
	private Style avatarStyle = Style.ROUND_SQUARE;
	@Column
	@Info(keyType = ConfigFieldType.COLOR)
	private Color avatarBorderColor = TRANSPARENT;
	@Column
	@Info(keyType = ConfigFieldType.INTEGER)
	private int avatarBorderWidth = 10;

	@Column
	@Info(keyType = ConfigFieldType.ENUM)
	private Style badgeStyle = Style.ROUND_SQUARE;
	@Column
	@Info(keyType = ConfigFieldType.COLOR)
	private Color badgeBorderColor = new Color(68, 140, 41, 255);
	@Column
	@Info(keyType = ConfigFieldType.INTEGER)
	private int badgeBorderWidth = 0;


	@Column
	@Info(keyType = ConfigFieldType.COLOR)
	private Color backgroundColor = new Color(30, 30, 30, 200);
	@Column
	@Info(keyType = ConfigFieldType.URL)
	private String backgroundImageURL;
	@Column
	@Info(keyType = ConfigFieldType.COLOR)
	private Color backgroundBorderColor = new Color(68, 140, 41, 255);
	@Column
	@Info(keyType = ConfigFieldType.INTEGER)
	private int backgroundBorderWidth = 10;

	@Column
	@Info(keyType = ConfigFieldType.COLOR)
	private Color fontColor = Color.WHITE;
	@Column
	@Info(keyType = ConfigFieldType.COLOR)
	private Color fontSecondaryColor = Color.GRAY;
	@Column
	@Info(keyType = ConfigFieldType.COLOR)
	private Color fontLevelColor = TRANSPARENT;

	@Column
	@Info(keyType = ConfigFieldType.ENUM)
	private RankColor fontLevelStyle = RankColor.ROLE;

	@Column
	@Info(keyType = ConfigFieldType.STRING)
	private String name = "*Unbenannt*";


	public CardProfileData(@NotNull SlimeBot bot, @NotNull UserSnowflake owner) {
		this.bot = bot;
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
		if (owner == null) return CardPermission.READ;
		if (owner.getIdLong() == user.getIdLong()) return CardPermission.WRITE;
		if (isPublic) return CardPermission.READ;
		return CardPermission.NONE;
	}

	@NotNull
	public CardProfileData createCopy(@NotNull UserSnowflake owner) {
		//Setting the id to null will make JavaUtils create a new row
		this.id = null;
		this.owner = owner;

		return this;
	}

	@NotNull
	@Override
	public String build(int index, @NotNull ListContext<? extends ListEntry> context) {
		return (index + 1) + ". **" + name + "** (" + id + "), von " + owner.getAsMention();
	}

	@NotNull
	public CardProfileData set(@NotNull String name, @NotNull String value) {
		try {
			Field field = getClass().getDeclaredField(name);
			field.setAccessible(true);

			if (value.isEmpty()) field.set(this, field.get(DEFAULT));
			else {
				ConfigFieldType type = field.getAnnotation(Info.class).keyType();

				if (!type.validate(field.getType(), value)) throw new ValidationException(null);
				field.set(this, type.parse(field.getType(), value));
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

			if (field.isAnnotationPresent(Info.class)) return field.getAnnotation(Info.class).keyType().toString(value);
			else return Objects.toString(value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	public String toString(@Nullable Guild guild) {
		Member m = guild != null ? guild.getMember(owner) : null;

		return (name.equals("*Unbenannt*") ? "Unbennant" : name) + " " + (guild != null ? (m != null ? "von " + m.getEffectiveName() + " " : "Unbekannt") : "") + "(" + id + ")";
	}

	@NotNull
	public CardRenderer render(@NotNull Member member) {
		return new CardRenderer(bot, this, member, null);
	}

	@NotNull
	public CardRenderer renderPreview(@NotNull Member member, int maxXp) {
		return new CardRenderer(bot, this, member, maxXp);
	}
}
