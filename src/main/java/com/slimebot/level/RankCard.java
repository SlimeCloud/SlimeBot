package com.slimebot.level;

import com.slimebot.graphic.CustomFont;
import com.slimebot.graphic.Graphic;
import com.slimebot.graphic.ImageUtil;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class RankCard extends Graphic {
    public final static Color barBackground = new Color(66, 155, 46, 200);
    public final static Color barForeground = new Color(105, 227, 73, 200);

    private final static Font font;

    static {
        try {
            font = CustomFont.getFont("Ubuntu.ttf", Font.BOLD, 80);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private final Level level;

    public RankCard(Level level) {
        super(2400, 400);
        this.level = level;
        finish();
    }

    @Override
    public void drawGraphic(Graphics2D graphics2D) throws Exception {
        User user = Main.jdaInstance.getUserById(level.user());

        int avatarWidth = height - 80;

        BufferedImage avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl()));

        avatar = ImageUtil.resize(avatar, avatarWidth, avatarWidth);
        avatar = ImageUtil.circle(avatar);

        graphics2D.drawImage(avatar, 0, 80, null);

        int xpRequired = Level.calculateRequiredXP(level.level() + 1);
        double percentage = (double) level.xp() / xpRequired;

        int maxBarSize = width - 160 - avatarWidth;
        int barSize = (int) (maxBarSize * percentage);

        graphics2D.setColor(barBackground);
        graphics2D.drawRoundRect(avatarWidth + 80, height - 120, maxBarSize, height - 360, height - 360, height - 360);

        graphics2D.setColor(barForeground);
        graphics2D.fillRoundRect(avatarWidth + 80, height - 120, barSize, height - 360, height - 360, height - 360);

        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(CustomFont.getFont(font, 104F));
        graphics2D.drawString(user.getEffectiveName(), avatarWidth + 100, height - 160);

        graphics2D.setFont(CustomFont.getFont(font, 80F));

        String s = level.xp() + "/" + xpRequired + " xp";
        graphics2D.drawString(s, width - graphics2D.getFontMetrics().stringWidth(s) - 80, height - 160);

        s = "Level: " + level.level();
        graphics2D.drawString(s, width - graphics2D.getFontMetrics().stringWidth(s) - 80, height - 320);
    }
}
