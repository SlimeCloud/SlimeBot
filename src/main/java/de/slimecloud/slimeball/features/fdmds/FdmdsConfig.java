package de.slimecloud.slimeball.features.fdmds;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FdmdsConfig extends ConfigCategory {
	@ConfigField(name = "Log-Kanal", command = "log", description = "Kanal, in dem Einreichungen zur Verifikation gesendet werden", type = ConfigFieldType.MESSAGE_CHANNEL, required = true)
	private long log;

	@ConfigField(name = "Kanal", command = "channel", description = "Kanal für FdmdS Umfragen", type = ConfigFieldType.MESSAGE_CHANNEL, required = true)
	private long channel;

	@ConfigField(name = "Benachrichtigungs-Rolle", command = "role", description = "Rolle, die bei neuen Umfragen benachrichtigt wird", type = ConfigFieldType.ROLE)
	private Long role;

	@NotNull
	public GuildMessageChannel getLogChannel() {
		return bot.getJda().getChannelById(GuildMessageChannel.class, log);
	}

	@NotNull
	public GuildMessageChannel getChannel() {
		return bot.getJda().getChannelById(GuildMessageChannel.class, channel);
	}

	@NotNull
	public Optional<Role> getRole() {
		return Optional.ofNullable(role).map(id -> bot.getJda().getRoleById(id));
	}
}
