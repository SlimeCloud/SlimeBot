package de.slimecloud.slimeball.features.birthday;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import lombok.Getter;

@Getter
public class BirthdayConfig extends ConfigCategory {

	@ConfigField(name = "annouceChat", command = "annouce-chat", description = "Der Chat in dem geburtstage angekündigt werden", type = ConfigFieldType.MESSAGE_CHANNEL)
	public long announceChat;

	@ConfigField(name = "reportChat", command = "report-chat", description = "Der chat in den eine warnung gesendet wird wenn ein nutzer unter 16 ist", type = ConfigFieldType.MESSAGE_CHANNEL)
	public long reportChat;

	@ConfigField(name = "birthdayRole", command = "role", description = "Die rolle die an nutzer vergeben wird wenn sie geburtstag haben", type = ConfigFieldType.ROLE)
	public long birthdayRole;

}
