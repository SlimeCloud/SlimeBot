package com.slimebot.graphic;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class CustomFont {

    public static Font getFont(String path, int style, float size) throws IOException, FontFormatException {
        if(path == null) return null;
        return getFont(path).deriveFont(style, size);
    }

    public static Font getFont(String path, Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) throws IOException, FontFormatException {
        if(path == null) return null;
        return getFont(path).deriveFont(attributes);
    }

    public static Font getFont(String path, int style, AffineTransform trans) throws IOException, FontFormatException {
        if(path == null) return null;
        return getFont(path).deriveFont(style, trans);
    }

    public static Font getFont(String path, AffineTransform trans) throws IOException, FontFormatException {
        if(path == null) return null;
        return getFont(path).deriveFont(trans);
    }

    public static Font getFont(String path, float size) throws IOException, FontFormatException {
        if(path == null) return null;
        return getFont(path).deriveFont(size);
    }

    public static Font getFont(String path, int style) throws IOException, FontFormatException {
        if(path == null) return null;
        return getFont(path).deriveFont(style);
    }

    public static Font getFont(String path) throws IOException, FontFormatException {
        if(path == null) return null;
        String[] args = new File(path).getName().split("\\.");
        String suffix = args[args.length - 1];
        return Font.createFont(getType(suffix), CustomFont.class.getResourceAsStream(path));
    }

    private static int getType(String suffix) {
        return switch(suffix) {
            case "ttf", "otf" -> Font.TRUETYPE_FONT;
            case "pfa", "pfb" -> Font.TYPE1_FONT;
            default -> 0;
        };
    }
}
