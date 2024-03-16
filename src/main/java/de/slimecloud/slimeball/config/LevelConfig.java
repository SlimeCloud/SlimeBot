package de.slimecloud.slimeball.config;

import de.slimecloud.slimeball.config.engine.Required;
import lombok.Getter;

@Getter
public class LevelConfig {
	@Required
	private String badgeFolder;

	@Required
	private String levelUpMessage;

	@Required
	private Integer messageCooldown;
	@Required
	private Double minMessageXP;
	@Required
	private Double maxMessageXP;
	@Required
	private Integer minWordLength;
	@Required
	private Double minWordXP;
	@Required
	private Double maxWordXP;

	@Required
	private Integer voiceLevelingInterval;
	@Required
	private Double minVoiceXP;
	@Required
	private Double maxVoiceXP;

	@Required
	private Integer maxUserProfiles;
}
