package de.slimecloud.slimeball.features.level;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.config.engine.Info;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class GuildLevelConfig extends ConfigCategory {
	@Info(minValue = 0.2)
	@ConfigField(name = "Level-Faktor", command = "multiplier", description = "Faktor, der auf alle Xp-Einnahmen berechnet wird", type = ConfigFieldType.NUMBER_UI)
	private final Double multiplier = 1.0;

	@ConfigField(name = "Kanal", command = "channel", description = "Kanal, in dem Level-Up Nachrichten gesendet werden", type = ConfigFieldType.MESSAGE_CHANNEL)
	private Long channel;

	@ConfigField(name = "Blockierte Kanäle", command = "blacklist", description = "List an Kanälen, die ignoriert werden", type = ConfigFieldType.ALL_CHANNEL)
	private final List<Long> channelBlacklist = new ArrayList<>();

	@ConfigField(name = "Level Rollen", command = "roles", description = "Rollen, die Mitglieder basierend auf ihrem Level bekommen", type = ConfigFieldType.ROLE)
	@Info(keyType = ConfigFieldType.INTEGER)
	private final Map<Integer, Long> levelRoles = new HashMap<>();

	@NotNull
	public Optional<GuildMessageChannel> getChannel() {
		return Optional.ofNullable(channel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}
}
