package com.slimebot.commands.config.setup.engine;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MessageFrameBase;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SetupMainFrame extends MessageFrameBase {
	private final List<ComponentRow> components;

	public SetupMainFrame(Menu menu, List<ComponentRow> components) {
		super(menu);

		this.components = components;
	}

	@Override
	public MessageEmbed getEmbed() {
		return new EmbedBuilder()
				.setTitle("\uD83D\uDD27 Einstellungs-Menü")
				.setColor(GuildConfig.getColor(menu.getGuild()))
				.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
				.setDescription("In diesem Menü kann die Konfiguration des Bots für diesen Server eingestellt werden. Nutze die Knöpfe um ein Untermenü zu öffnen, in dem du dann Einstellungen für die Entsprechende Funktion vornehmen " +
						"kannst")
				.build();
	}

	@Override
	public Collection<ComponentRow> getComponents() {
		Collection<ComponentRow> result = new ArrayList<>(components);
		result.add(new ButtonComponent("close", ButtonColor.RED, "Menü Schließen").addHandler(menu -> menu.close()));
		return result;
	}
}
