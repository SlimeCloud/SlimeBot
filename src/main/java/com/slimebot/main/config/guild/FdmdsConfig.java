package com.slimebot.main.config.guild;

import com.slimebot.main.config.guild.engine.ChannelField;
import com.slimebot.main.config.guild.engine.RoleField;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Optional;

public class FdmdsConfig {
	@ChannelField(title = "Fdmds Kanal", description = "In diesen Kanal werden die Fdmds fragen gesendet")
	public Long channel;
	@ChannelField(title = "Fdmds Log Kanal", description = "In diesen Kanal werden Fdmds Nachrichten zur Verifikation gesendet")
	public Long logChannel;

	@RoleField(title = "Fdmds Rolle", description = "Die Rolle, die bei neuen Fdmds Fragen erwähnt wird")
	public Long role;

	public Optional<GuildMessageChannel> getChannel() {
		return GuildConfig.getChannel(channel);
	}

	public Optional<GuildMessageChannel> getLogChannel() {
		return GuildConfig.getChannel(logChannel);
	}

	public Optional<Role> getRole() {
		return GuildConfig.getRole(role);
	}
}
