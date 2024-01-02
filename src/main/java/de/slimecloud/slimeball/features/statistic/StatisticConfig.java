package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.config.engine.KeyType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class StatisticConfig extends ConfigCategory {
	//TODO new config system as soon as it is ready


	@ConfigField(name = "MemberCount Kanal", command = "member_channel", description = "-", type = ConfigFieldType.VOICE_CHANNEL)
	private Long memberCountChannel;

	@ConfigField(name = "MemberCount Formatierung", command = "member_format", description = "-", type = ConfigFieldType.STRING)
	private String memberCountFormat = "Total Members: %members%";

	@ConfigField(name = "RoleMemberCount Default Formatierung", command = "default_format", description = "-", type = ConfigFieldType.STRING)
	private String defaultRoleFormat = "%role_name%: %members%";

	@ConfigField(name = "RoleMemberCount Kanal", command = "role_channel", description = "-", type = ConfigFieldType.VOICE_CHANNEL)
	@KeyType(ConfigFieldType.ROLE)
	private Map<Long, Long> roleMemberCountChannel = new HashMap<>();

	@ConfigField(name = "RoleMemberCount Formatierung", command = "role_format", description = "-", type = ConfigFieldType.STRING)
	@KeyType(ConfigFieldType.ROLE)
	private Map<Long, String> roleMemberCountFormat = new HashMap<>();


	@Override
	public void update() {

	}
}
