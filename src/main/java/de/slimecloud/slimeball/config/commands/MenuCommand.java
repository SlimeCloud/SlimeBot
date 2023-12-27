package de.slimecloud.slimeball.config.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "menu", description = "Öffnet ein Menü für die konfiguration")
public class MenuCommand {
	private final MessageMenu menu;

	public MenuCommand(@NotNull SlimeBot bot, @NotNull UIManager manager) {
		menu = manager.createMenu(
				"setup",
				MessageRenderer.embed(state -> new EmbedBuilder()
						.setDescription("Menü noch nicht implementiert, wird demnächst hinzugefügt") //TODO
						.build()
				),
				ComponentRow.ofMany(

				)
		);
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event) {
		menu.display(event);
	}
}
