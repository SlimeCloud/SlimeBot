package com.slimebot.graphic;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class CustomFont {

    public static Font getFont(String name, int style, float size) throws IOException, FontFormatException {
        if(name == null) return null;
        return getFont(name).deriveFont(style, size);
    }

    public static Font getFont(String name, Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) throws IOException, FontFormatException {
        if(name == null) return null;
        return getFont(name).deriveFont(attributes);
    }

    public static Font getFont(String name, int style, AffineTransform trans) throws IOException, FontFormatException {
        if(name == null) return null;
        return getFont(name).deriveFont(style, trans);
    }

    public static Font getFont(String name, AffineTransform trans) throws IOException, FontFormatException {
        if(name == null) return null;
        return getFont(name).deriveFont(trans);
    }

    public static Font getFont(String name, float size) throws IOException, FontFormatException {
        if(name == null) return null;
        return getFont(name).deriveFont(size);
    }

    public static Font getFont(String name, int style) throws IOException, FontFormatException {
        if(name == null) return null;
        return getFont(name).deriveFont(style);
    }

    public static Font getFont(String name) throws IOException, FontFormatException {
        if(name == null) return null;
        String[] args = new File(name).getName().split("\\.");
        String suffix = args[args.length - 1];
        return Font.createFont(getType(suffix), CustomFont.class.getClassLoader().getResourceAsStream("font/" + name));
    }

    private static int getType(String suffix) {
        return switch(suffix) {
            case "ttf", "otf" -> Font.TRUETYPE_FONT;
            case "pfa", "pfb" -> Font.TYPE1_FONT;
            default -> 0;
        };
    }
}
