package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.config.engine.Info;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
public class StatisticConfig extends ConfigCategory {
	@Setter
	@ConfigField(name = "MemberCount Kanal", command = "member_channel", description = "Kanal, der die aktuelle Anzahl an Server-Mitgliedern anzeigt", type = ConfigFieldType.VOICE_CHANNEL)
	private Long memberCountChannel;

	@ConfigField(name = "MemberCount Formatierung", command = "member_format", description = "Format für die Mitgliederanzahl-Anzeige. \nPlaceholder: %members%", type = ConfigFieldType.STRING)
	private String memberCountFormat = "Total Members: %members%";


	@ConfigField(name = "RoleMemberCount Default Formatierung", command = "default_format", description = "Standard Format für Rollen-Mitgliederanzahl-Anzeigen. \nPlaceholders: %role_name%, %members%", type = ConfigFieldType.STRING)
	private String defaultRoleFormat = "%role_name%: %members%";

	@ConfigField(name = "RoleMemberCount Kanal", command = "role_channel", description = "Kanäle, in denen Die Mitgliederanzahl mit einer bestimmten Rolle angezeigt werden", type = ConfigFieldType.ROLE)
	@Info(keyType = ConfigFieldType.VOICE_CHANNEL)
	private Map<Long, Long> roleMemberCountChannel = new HashMap<>();

	@ConfigField(name = "RoleMemberCount Formatierung", command = "role_format", description = "Format für die Rollen-Mitgliederanzahl-Anzeigen. \nPlaceholders: %role_name%, %members%", type = ConfigFieldType.STRING)
	@Info(keyType = ConfigFieldType.VOICE_CHANNEL)
	private Map<Long, String> roleMemberCountFormat = new HashMap<>();

	@Override
	public void update(@NotNull Guild guild) {
		bot.getMemberCount().update(this, guild);
		bot.getRoleMemberCount().update(this, guild);
	}
}
