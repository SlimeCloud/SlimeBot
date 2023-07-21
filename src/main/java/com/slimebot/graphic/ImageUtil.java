package com.slimebot.graphic;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class ImageUtil {

    public static BufferedImage circle(BufferedImage image) {
        int width = image.getWidth();
        BufferedImage circle = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circle.createGraphics();
        g2d.setClip(new Ellipse2D.Float(0, 0, width, width));
        g2d.drawImage(image, 0, 0, width, width, null);
        g2d.dispose();
        return circle;
    }

    public static BufferedImage resize(BufferedImage image, int width, int height) {
        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage result = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        return result;
    }
}
