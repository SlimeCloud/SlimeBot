package com.slimebot.commands.level.card.frame;

import com.slimebot.commands.level.card.CardComponent;
import com.slimebot.level.Level;
import com.slimebot.level.RankCard;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MessageFrameBase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public abstract class CardFrame extends MessageFrameBase {
	protected CardComponent COMPONENTS;
	public final CardComponent.Part part;
	private final String title;

	public CardFrame(Menu menu, CardComponent.Part part, String title) {
		super(menu);
		this.part = part;
		this.title = title;
	}

	@Override
	public void setup() {
		COMPONENTS = CardComponent.fromMember(menu.getMember());
	}

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
