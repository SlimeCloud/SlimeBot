package com.slimebot.commands.level.card.frame;

import com.slimebot.commands.level.card.CardComponent;
import com.slimebot.main.Main;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class ResetWarningFrame extends CardFrame {
	public ResetWarningFrame(Menu menu) {
		super(menu, null, "Reset");
	}

	@Override
	public EmbedBuilder buildEmbed() {
		return super.buildEmbed()
				.setTitle("Warnung!")
				.setDescription("""
						Bist du sicher das du deine rankcard zurücksetzen möchtest?
						Das zurücksetzen **kann nicht rückgändig gemacht werden!**
													
						**Unwiderruflich zurücksetzen?**
						""")
				.setColor(new Color(218, 55, 60));
	}

	@Override
	public Collection<ComponentRow> getComponents(CardComponent COMPONENTS) {
		return List.of(
				ComponentRow.of(COMPONENTS.BACK(),
						new ButtonComponent("reset", ButtonColor.RED, Emoji.fromUnicode("✔")).addHandler((m, event) ->
								Main.database.run(handle -> handle.createUpdate("delete from cardprofile where guild = :guild and \"user\" = :user")
										.bind("guild", event.getGuild().getIdLong())
										.bind("user", event.getUser().getIdLong())
								)
						))
		);
	}
}
