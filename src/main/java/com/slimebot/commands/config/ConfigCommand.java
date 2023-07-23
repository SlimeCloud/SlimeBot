package com.slimebot.commands.config;

import com.slimebot.main.CommandPermission;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.commands.CommandManager;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.WhenFinished;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.Consumer;

/**
 * Dieser Befehl wird verwendet, um es dem Team einfach zu ermöglichen, die Konfiguration des Servers anzupassen.
 * Für jede "Kategorie" an Konfiguration gibt es einen eigenen Unterbefehl.
 * Wenn du selbst ein Konfigurationsfeld mit Befehl hinzufügen möchtest, schau dir vorher an, wie dies bei bestehenden Konfigurationsbefehlen gemacht wurde.
 */
@ApplicationCommand(name = "config", description = "Verwaltet die Bot-Konfiguration für diesen Server", guildOnly = true, subcommands = {GuildConfigCommand.class, FdmdsConfigCommand.class, StaffConfigCommand.class, SpotifyConfigCommand.class})
public class ConfigCommand {
	public CommandPermission permission = CommandPermission.TEAM;

	/**
	 * Gibt dir im `handler` Zugriff auf die Konfiguration eines Servers und speichert sie anschließend.
	 * @param guild Der Server, dessen Konfiguration du verändern möchtest
	 * @param handler Der handler, in dem du die Konfiguration anpassen kannst.
	 */
	public static void updateField(Guild guild, Consumer<GuildConfig> handler) {
		GuildConfig config = GuildConfig.getConfig(guild);
		handler.accept(config);
		config.save();
	}

	@WhenFinished
	public void setup(CommandManager<?> cmdMan) {

	}
}
