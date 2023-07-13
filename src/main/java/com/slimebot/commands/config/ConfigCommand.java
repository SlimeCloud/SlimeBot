package com.slimebot.commands.config;

import com.slimebot.main.CommandPermission;
import de.mineking.discord.commands.annotated.ApplicationCommand;

@ApplicationCommand(name = "config", description = "Verwaltet die Bot-Konfiguration für diesen Server", guildOnly = true, subcommands = {GuildConfigCommand.class, FdmdsConfigCommand.class, StaffConfigCommand.class})
public class ConfigCommand {
	public CommandPermission permission = CommandPermission.TEAM;
}
