package com.slimebot.level.profile;

import com.slimebot.database.DataClass;
import com.slimebot.database.Key;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

@Data
@Accessors(chain = true)
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class CardProfile extends DataClass {
	public static final int TRANSPARENT = 0;
	private static final CardProfile DEFAULT = new CardProfile(0, 0);

	@Key
	private final long guild;
	@Key
	private final long user;

	private int progressBarColor = new Color(105, 227, 73, 200).getRGB();
	private int progressBarBGColor = new Color(150, 150, 150, 50).getRGB();
	private Style progressBarStyle = Style.ROUND;
	private int progressBarBorderColor = new Color(68, 140, 41, 255).getRGB();
	private int progressBarBorderWidth = 15;

	private Style avatarStyle = Style.ROUND;
	private int avatarBorderColor = TRANSPARENT;
	private int avatarBorderWidth = 0;

	private int backgroundColor = TRANSPARENT;
	private String backgroundImageURL = "";
	private int backgroundBorderColor = TRANSPARENT;
	private int backgroundBorderWidth = 0;

	public CardProfile(long guild, long user) {
		this.guild = guild;
		this.user = user;
	}

	private Color getColor(int rgba) {
		return new Color(rgba, true);
	}

	public Progressbar getProgressBar() {
		return new Progressbar(getColor(progressBarColor), getColor(progressBarBGColor), progressBarStyle, new Border(getColor(progressBarBorderColor), progressBarBorderWidth));
	}

	public Avatar getAvatar() {
		return new Avatar(avatarStyle, new Border(getColor(avatarBorderColor), avatarBorderWidth));
	}

	public Background getBackground() {
		return new Background(getColor(backgroundColor), backgroundImageURL, new Border(getColor(backgroundBorderColor), backgroundBorderWidth));
	}

	public void reset(String name) {
		try {
			Field field = getClass().getDeclaredField(name);
			if (!isValid(field)) return;
			field.setAccessible(true);
			field.set(this, field.get(DEFAULT));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			logger.error("error on reset '" + name + "'", e);
		}
	}

	public static CardProfile loadProfile(Member member) {
		Supplier<CardProfile> sup = () -> new CardProfile(member.getGuild().getIdLong(), member.getIdLong());
		return DataClass.load(
				sup,
				Map.of(
						"guild", member.getGuild().getIdLong(),
						"user", member.getIdLong()
				)
		).orElseGet(sup);
	}
}
