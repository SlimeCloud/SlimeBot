package com.slimebot.graphic;

import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class Graphic {
	protected final int width;
	protected final int height;
	private final BufferedImage image;

	protected Graphic(int width, int height) {
		this.width = width;
		this.height = height;
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	protected void finish() {
		try {
			Graphics2D graphics = image.createGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			drawGraphic(graphics);
			graphics.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void drawGraphic(Graphics2D graphics2D) throws Exception;

	public FileUpload getFile() throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", bos);
			return FileUpload.fromData(bos.toByteArray(), "image.png");
		}
	}
}
