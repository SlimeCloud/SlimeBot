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

    public static BufferedImage fill(BufferedImage image, Color color) {
        Graphics2D g2d = image.createGraphics();
        g2d.setPaint(color);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.dispose();
        return image;
    }

    public static BufferedImage merge(BufferedImage img1, BufferedImage img2) {
        int w = Math.max(img1.getWidth(), img2.getWidth());
        int h = Math.max(img1.getHeight(), img2.getHeight());
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(img1, 0, 0, null);
        g2d.drawImage(img2, 0, 0, null);
        g2d.dispose();
        return result;
    }

    public static BufferedImage merge(BufferedImage base, BufferedImage toMerge, int x, int y) {
        Graphics2D g2d = base.createGraphics();
        g2d.drawImage(toMerge, x, y, null);
        g2d.dispose();
        return base;
    }
}
