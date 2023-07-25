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

    private final static Font font;

    static {
        Font font_;
        try {
            font_ = CustomFont.getFont("Ubuntu.ttf", Font.BOLD, 80);
        } catch(IOException | FontFormatException e) {
            e.printStackTrace();
            font_ = new Font("Arial", Font.BOLD, 80);
        }
        font = font_;
    }

    private final User user;
    private final int level;
    private final int xp;

    public RankCard(Level level) {
        super(2400, 400);
        this.user = Main.jdaInstance.getUserById(level.userId());
        this.level = level.level();
        this.xp = level.xp();
        finish();
    }

    @Override
    public void drawGraphic(Graphics2D graphics2D) throws Exception {
        int avatarWidth = height - 80;
        BufferedImage avatar = ImageIO.read(new URL(getAvatarURL(user)));
        avatar = ImageUtil.resize(avatar, avatarWidth, avatarWidth);
        avatar = ImageUtil.circle(avatar);
        graphics2D.drawImage(avatar, 0, 80, null);

        int xpRequired = Level.calculateRequiredXP(level + 1);
        double percentage = (double) xp / xpRequired;
        int maxBarSize = width - 160 - avatarWidth;
        int barSize = (int) (maxBarSize * percentage);

        graphics2D.setColor(new Color(66, 155, 46, 200));
        graphics2D.drawRoundRect(avatarWidth + 80, height - 120, maxBarSize, height - 360, height - 360, height - 360);
        graphics2D.setColor(new Color(105, 227, 73, 200));
        graphics2D.fillRoundRect(avatarWidth + 80, height - 120, barSize, height - 360, height - 360, height - 360);

        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(CustomFont.getFont(font, 104F));
        graphics2D.drawString(user.getEffectiveName(), avatarWidth + 100, height - 160);

        graphics2D.setFont(CustomFont.getFont(font, 80F));
        String s = xp + "/" + xpRequired + " xp";
        graphics2D.drawString(s, width - graphics2D.getFontMetrics().stringWidth(s) - 80, height - 160);
        s = "Level: " + level;
        graphics2D.drawString(s, width - graphics2D.getFontMetrics().stringWidth(s) - 80, height - 320);
    }

    private String getAvatarURL(User user) {
        String url = user.getAvatarUrl();
        return url==null ? user.getDefaultAvatarUrl() : url;
    }
}
