package com.slimebot.commands.level.card.frame;

import com.slimebot.commands.level.card.CardComponent;
import com.slimebot.level.Level;
import com.slimebot.level.RankCard;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MessageFrameBase;
import de.mineking.discord.ui.components.ComponentRow;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.util.Collection;

public abstract class CardFrame extends MessageFrameBase {

    private CardComponent COMPONENTS;
    public final CardComponent.Part part;
    private final String title;

    public CardFrame(Menu menu, CardComponent.Part part, String title) {
        super(menu);
        this.part = part;
        this.title = title;
    }

    @Override
    public final Collection<ComponentRow> getComponents() {
        if (COMPONENTS == null) COMPONENTS = CardComponent.fromMember(menu.getMember());
        return getComponents(COMPONENTS);
    }

    protected abstract Collection<ComponentRow> getComponents(CardComponent COMPONENTS);

    @Override
    public final MessageEditBuilder buildMessage() {
        return super.buildMessage().setFiles(new RankCard(Level.getLevel(menu.getMember())).getFile());
    }

    protected EmbedBuilder buildEmbed() {
        return new EmbedBuilder()
                .setColor(GuildConfig.getColor(menu.getGuild()))
                .setTitle(title)
                .setImage("attachment://image.png");
    }

    @Override
    public final MessageEmbed getEmbed() {
        return buildEmbed().build();
    }
}
