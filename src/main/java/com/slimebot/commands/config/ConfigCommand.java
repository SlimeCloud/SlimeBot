package com.slimebot.commands.config;

import com.slimebot.main.CommandPermission;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.Consumer;

@ApplicationCommand(name = "config", description = "Verwaltet die Bot-Konfiguration für diesen Server", guildOnly = true, subcommands = {GuildConfigCommand.class, FdmdsConfigCommand.class, StaffConfigCommand.class, SpotifyConfigCommand.class})
public class ConfigCommand {
	public CommandPermission permission = CommandPermission.TEAM;

	public static void updateField(Guild guild, Consumer<GuildConfig> handler) {
		GuildConfig config = GuildConfig.getConfig(guild);
		handler.accept(config);
		config.save();
	}
}
