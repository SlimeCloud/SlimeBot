package com.slimebot.graphic;

import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class Graphic {
	private Logger logger;

	protected final int width;
	protected final int height;
	private final BufferedImage image;

	protected Graphic(int width, int height) {
		this.width = width;
		this.height = height;
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		this.logger = LoggerFactory.getLogger(getClass());
	}

	protected void finish() {
		try {
			Graphics2D graphics = image.createGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			drawGraphic(graphics);
			graphics.dispose();
		} catch (IOException e) {
			logger.error("Failed to render graphic", e);
		}
	}

	protected abstract void drawGraphic(Graphics2D graphics2D) throws IOException;

	public FileUpload getFile() {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", bos);
			return FileUpload.fromData(bos.toByteArray(), "image.png");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
