package com.slimebot.util;

import java.awt.*;
import java.util.Arrays;

public class ColorUtil {

	public static String toString(Color color) {
		return """
				red: %s
				green: %s
				blue: %s
				alpha: %s
				rgb: %s
				hex: %s
				""".formatted(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), color.getRGB(), toHex(color));
	}

	public static String toHex(Color color) {
		String hex;
		if (color.getAlpha()==255) hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
		else hex = String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		boolean flag = true;
		for (int i = 0; i < hex.length(); i+=2) {
			if (hex.charAt(i) != hex.charAt(i + 1)) {
				flag = false;
				break;
			}
		}
		if (flag) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < hex.length(); i+=2) sb.append(hex.charAt(i));
			hex = sb.toString();
		}
		return hex;
	}

	public static Color parseColor(String color) {
		if (color.startsWith("#")) return parseHex(color);
		if (color.contains(",")) return parseRGBA(color);
		if (Util.isNumeric(color)) return parseRGBACode(color);
		return parseColorName(color);
	}

	public static Color parseRGBA(String rgba) {
		String[] values = rgba.split(",");
		values = Arrays.stream(values)
				.map(String::strip)
				.toArray(String[]::new);
		if (values.length!=3 && values.length!=4) return null;
		int r = Integer.parseInt(values[0]);
		int g = Integer.parseInt(values[1]);
		int b = Integer.parseInt(values[2]);
		int a = values.length==4 ? Integer.parseInt(values[3]) : 255;
		return new Color(r, g, b, a);
	}

	public static Color parseRGBACode(String code) {
		return new Color(Integer.parseInt(code.strip()), true);
	}

	public static Color parseColorName(String name) {
		try {
			return (Color) Color.class.getField(name.strip().toUpperCase()).get(null);
		} catch (IllegalAccessException | NoSuchFieldException ignored) {}
		return null;
	}

	public static Color parseHex(String hex) {
		hex = hex.replace("#", "").strip();
		return switch (hex.length()) {
			case 3, 4 -> {
				char[] chars = new char[hex.length()*2];
				for (int i = 0; i < hex.length(); i+=2) {
					chars[i] = hex.charAt(i);
					chars[i+1] = hex.charAt(i);
				}
				System.out.println(new String(chars));
				yield parseHex(new String(chars));
			}
			case 6 -> new Color(
					Integer.valueOf(hex.substring(0, 2), 16),
					Integer.valueOf(hex.substring(2, 4), 16),
					Integer.valueOf(hex.substring(4, 6), 16));
			case 8 -> new Color(
					Integer.valueOf(hex.substring(0, 2), 16),
					Integer.valueOf(hex.substring(2, 4), 16),
					Integer.valueOf(hex.substring(4, 6), 16),
					Integer.valueOf(hex.substring(6, 8), 16));
			default -> null;
		};
	}

}
