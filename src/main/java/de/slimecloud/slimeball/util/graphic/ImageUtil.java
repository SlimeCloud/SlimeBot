package de.slimecloud.slimeball.util.graphic;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@UtilityClass
public class ImageUtil {
	@Nullable
	public BufferedImage readFromUrl(@Nullable String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) return null;
		try {
			URL url = new URL(imageUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/117.0");

			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);

			return ImageIO.read(con.getInputStream());
		} catch (IOException ignore) {
			return null;
		}
	}

	@NotNull
	public BufferedImage resize(@NotNull BufferedImage image, int width, int height) {
		Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

		BufferedImage result = new BufferedImage(width, height, image.getType());
		Graphics2D graphics = result.createGraphics();

		graphics.drawImage(scaled, 0, 0, null);
		graphics.dispose();

		return result;
	}
}
