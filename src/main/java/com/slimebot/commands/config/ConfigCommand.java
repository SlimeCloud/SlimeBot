package com.slimebot.commands.config;

import com.slimebot.main.CommandPermission;
import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import net.dv8tion.jda.api.entities.Guild;

@ApplicationCommand(name = "config", description = "Verwaltet die Bot-Konfiguration für diesen Server", guildOnly = true, subcommands = {GuildConfigCommand.class, FdmdsConfigCommand.class, StaffConfigCommand.class, SpotifyConfigCommand.class})
public class ConfigCommand {
	public CommandPermission permission = CommandPermission.TEAM;

	public static void setField(Guild guild, DatabaseField field, Object value) {
		Main.database.run(handle -> handle.createUpdate("insert into " + field.table + "(guild, " + field.columnName + ") values(:guild, :value) on conflict (guild) do update set " + field.columnName + " = :value")
				.bind("guild", guild.getIdLong())
				.bind("value", value)
				.execute()
		);
	}
}
