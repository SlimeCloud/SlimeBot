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

	@ConfigField(name = "MemberCount Kanal", command = "member_count_channel", description = "Der kanal der für die MemberCount Statistic verwendet werden soll", type = ConfigFieldType.VOICE_CHANNEL)
	private Long memberCountChannel;

	@ConfigField(name = "MemberCount Formatierung", command = "member_count_format", description = "Die Formatierung für den MemberCount Statistic Kanal", type = ConfigFieldType.STRING)
	private String memberCountFormat = "Total Members: %members%";

	@ConfigField(name = "RoleMemberCount Default Formatierung", command = "role_member_count_default_format", description = "Die Standard formatierung für RoleMemberCount kanäle, sollte keine formatierung für die rolle festgelegt sein", type = ConfigFieldType.STRING)
	private String defaultRoleFormat = "%role_name%: %members%";

	@ConfigField(name = "RoleMemberCount Kanal", command = "role_member_count_channel", description = "Der kanal der für die MemberCount Statistic verwendet werden soll", type = ConfigFieldType.VOICE_CHANNEL)
	@KeyType(ConfigFieldType.ROLE)
	private Map<Long, Long> roleMemberCountChannel = new HashMap<>();

	@ConfigField(name = "RoleMemberCount Formatierung", command = "role_member_count_format", description = "Die Formatierung für den RoleMemberCount Statistic Kanal", type = ConfigFieldType.STRING)
	@KeyType(ConfigFieldType.ROLE)
	private Map<Long, String> roleMemberCountFormat = new HashMap<>();




}
