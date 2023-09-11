package com.slimebot.util;

import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ColorUtil {

    public static String toString(Color color) {
        if (color == null) return "*null*";
        return toHex(color);
		/*return """
				red: %s
				green: %s
				blue: %s
				alpha: %s
				rgb: %s
				hex: %s
				""".formatted(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), color.getRGB(), toHex(color));*/
    }

    public static String toHex(Color color) {
        if (color == null) return "#";
        String hex;
        if (color.getAlpha() == 255)
            hex = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        else
            hex = String.format("%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

        boolean flag = true;
        for (int i = 0; i < hex.length(); i += 2) {
            if (hex.charAt(i) != hex.charAt(i + 1)) {
                flag = false;
                break;
            }
        }
        if (flag) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hex.length(); i += 2) sb.append(hex.charAt(i));
            hex = sb.toString();
        }
        return "#" + hex;
    }

    public static @Nullable Color parseColor(String color) {
        if (color == null) return null;
        color = color.replaceAll("\\s", "");
        if (color.startsWith("#")) return parseHex(color);
        if (color.contains(",")) return parseRGBA(color);
        if (Util.isNumeric(color)) return parseRGBACode(color);
        return parseColorName(color);
    }

    public static @Nullable Color parseRGBA(String rgba) {
        if (rgba == null) return null;
        String[] values = rgba.split(",");
        if (values.length != 3 && values.length != 4) return null;
        int r = Integer.parseInt(values[0]);
        int g = Integer.parseInt(values[1]);
        int b = Integer.parseInt(values[2]);
        int a = values.length == 4 ? Integer.parseInt(values[3]) : 255;
        return new Color(r, g, b, a);
    }

    public static @Nullable Color parseRGBACode(String code) {
        if (code == null) return null;
        return new Color(Integer.parseInt(code), true);
    }

    public static @Nullable Color parseColorName(String name) {
        try {
            return (Color) Color.class.getField(String.join("_", Util.parseCamelCase(name)).toUpperCase()).get(null);
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }
        return null;
    }

    public static @Nullable Color parseHex(String hex) {
        if (hex == null) return null;
        hex = hex.substring(1);
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
