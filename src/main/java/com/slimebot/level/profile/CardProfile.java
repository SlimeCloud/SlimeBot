package com.slimebot.level.profile;

import com.slimebot.database.DataClass;
import com.slimebot.database.Key;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class CardProfile extends DataClass {

	public static final transient int SQUARE = 0;
	public static final transient int ROUND = 1;

	public static final transient int TRANSPARENT = new Color(0, 0, 0, 0).getRGB();

	@Key
	private final long guild;
	@Key
	private final long user;

	private int progressBarColor;
	private int progressBarBGColor;
	private int progressBarStyle;
	private int progressBarBorderColor;
	private int progressBarBorderWidth;

	private int avatarStyle;
	private int avatarBorderColor;
	private int avatarBorderWidth;

	private int backgroundColor;
	private String backgroundImageURL;
	private int backgroundBorderColor;
	private int backgroundBorderWidth;

	public CardProfile(long guild, long user) {
		this.guild = guild;
		this.user = user;

		this.progressBarColor = new Color(105, 227, 73, 200).getRGB();
		this.progressBarBGColor = new Color(150, 150, 150, 50).getRGB();
		this.progressBarStyle = ROUND;
		this.progressBarBorderColor = new Color(68, 140, 41, 255).getRGB();
		this.progressBarBorderWidth = 15;

		this.avatarStyle = ROUND;
		this.avatarBorderColor = TRANSPARENT;
		this.avatarBorderWidth = 0;

		this.backgroundColor = TRANSPARENT;
		this.backgroundImageURL = "";
		this.backgroundBorderColor = TRANSPARENT;
		this.backgroundBorderWidth = 0;
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
}
