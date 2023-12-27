package de.slimecloud.slimeball.util.graphic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class CustomFont {
	@NotNull
	public static Font getFont(@NotNull Font font, int style) {
		return font.deriveFont(style);
	}

	@NotNull
	public static Font getFont(@NotNull Font font, float size) {
		return font.deriveFont(size);
	}

	@NotNull
	public static Font getFont(@NotNull Font font, int style, float size) {
		return font.deriveFont(style).deriveFont(size);
	}

	@NotNull
	public static Font getFont(@NotNull String name, int style, float size) throws IOException, FontFormatException {
		return getFont(name).deriveFont(style, size);
	}

	@NotNull
	public static Font getFont(@NotNull String name, @Nullable Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) throws IOException, FontFormatException {
		return getFont(name).deriveFont(attributes);
	}

	@NotNull
	public static Font getFont(@NotNull String name, int style, @NotNull AffineTransform trans) throws IOException, FontFormatException {
		return getFont(name).deriveFont(style, trans);
	}

	@NotNull
	public static Font getFont(@NotNull String name, @NotNull AffineTransform trans) throws IOException, FontFormatException {
		return getFont(name).deriveFont(trans);
	}

	@NotNull
	public static Font getFont(@NotNull String name, float size) throws IOException, FontFormatException {
		return getFont(name).deriveFont(size);
	}

	@NotNull
	public static Font getFont(@NotNull String name, int style) throws IOException, FontFormatException {
		return getFont(name).deriveFont(style);
	}

	@NotNull
	public static Font getFont(@NotNull String name) throws IOException, FontFormatException {
		String[] args = new File(name).getName().split("\\.");
		String suffix = args[args.length - 1];

		return Font.createFont(getType(suffix), CustomFont.class.getClassLoader().getResourceAsStream("font/" + name));
	}

	private static int getType(@NotNull String suffix) {
		return switch (suffix) {
			case "ttf", "otf" -> Font.TRUETYPE_FONT;
			case "pfa", "pfb" -> Font.TYPE1_FONT;
			default -> 0;
		};
	}
}
