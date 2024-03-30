package de.slimecloud.slimeball.util;

import de.slimecloud.slimeball.config.engine.ValidationException;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@UtilityClass
public class ColorUtil {
	@NotNull
	public String extract(@NotNull OptionMapping value) {
		try {
			if (parseColor(value.getAsString()) != null) value.getAsString();
			throw new ValidationException(null);
		} catch (Exception e) {
			throw new ValidationException(e);
		}
	}

	@NotNull
	public String toString(@Nullable Color color) {
		if (color == null) return "*null*";
		if (color.getAlpha() == 0) return "*transparent*";

		return toHex(color);
	}

	@NotNull
	public Color ofCode(int code) {
		return new Color(code, true);
	}

	@NotNull
	public String toHex(@Nullable Color color) {
		if (color == null) return "*null*";

		String hex = color.getAlpha() == 255
				? String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue())
				: String.format("%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

		if (hex.matches("((?<a>.)\\k<a>)+")) hex = hex.replaceAll("(.).", "$1");

		return "#" + hex;
	}

	@Contract("null -> null")
	@Nullable
	public Color parseColor(@Nullable String color) {
		if (color == null) return null;

		try {
			color = color.replaceAll("\\s", "");

			if (color.startsWith("#")) return parseHex(color);
			if (color.contains(",")) return parseRGBA(color);
			if (StringUtil.isNumeric(color)) return parseRGBACode(color);
		} catch (NumberFormatException e) {
			return null;
		}

		return parseColorName(color);
	}

	@Contract("null -> null")
	@Nullable
	public Color parseRGBA(@Nullable String rgba) {
		if (rgba == null) return null;

		String[] values = rgba.split(",");
		if (values.length != 3 && values.length != 4) return null;

		int r = Integer.parseInt(values[0]);
		int g = Integer.parseInt(values[1]);
		int b = Integer.parseInt(values[2]);
		int a = values.length == 4 ? Integer.parseInt(values[3]) : 255;

		return new Color(r, g, b, a);
	}

	@Contract("null -> null")
	@Nullable
	public Color parseRGBACode(@Nullable String code) {
		if (code == null) return null;
		return new Color(Integer.parseInt(code), true);
	}

	@Nullable
	public Color parseColorName(@NotNull String name) {
		try {
			return (Color) Color.class.getField(String.join("_", StringUtil.parseCamelCase(name)).toUpperCase()).get(null);
		} catch (IllegalAccessException | NoSuchFieldException ignored) {
			return null;
		}
	}

	@Contract("null -> null")
	@Nullable
	public Color parseHex(@Nullable String hex) {
		if (hex == null) return null;
		if (hex.startsWith("#")) hex = hex.substring(1);

		return switch (hex.length()) {
			case 3, 4 -> {
				char[] chars = new char[hex.length() * 2];
				for (int i = 0; i < hex.length(); i++) {
					chars[i * 2] = hex.charAt(i);
					chars[(i * 2) + 1] = hex.charAt(i);
				}
				yield parseHex(new String(chars));
			}
			case 6 -> new Color(
					Integer.valueOf(hex.substring(0, 2), 16),
					Integer.valueOf(hex.substring(2, 4), 16),
					Integer.valueOf(hex.substring(4, 6), 16)
			);
			case 8 -> new Color(
					Integer.valueOf(hex.substring(0, 2), 16),
					Integer.valueOf(hex.substring(2, 4), 16),
					Integer.valueOf(hex.substring(4, 6), 16),
					Integer.valueOf(hex.substring(6, 8), 16)
			);
			default -> null;
		};
	}
}
