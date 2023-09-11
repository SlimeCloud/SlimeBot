package com.slimebot.commands.config.setup.engine;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MessageFrameBase;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Optional;

@Getter
public abstract class CustomSetupFrame extends MessageFrameBase {
	protected final long guild;
	protected final String title;
	protected final String description;

	private final String name;

	protected CustomSetupFrame(String name, Menu menu, long guild, String title, String description) {
		super(menu);
		this.name = name;

		this.guild = guild;
		this.title = title;
		this.description = description;
	}

	public abstract Optional<String> getValue(GuildConfig config);

	@Override
	public MessageEmbed getEmbed() {
		return new EmbedBuilder()
				.setTitle(title)
				.setColor(GuildConfig.getColor(guild))
				.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
				.setDescription(description)
				.addField("Aktueller Wert", getValue(GuildConfig.getConfig(guild)).orElse("*Kein Wert*"), false)
				.build();
	}
}
