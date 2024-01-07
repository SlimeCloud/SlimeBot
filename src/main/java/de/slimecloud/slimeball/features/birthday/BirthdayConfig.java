package de.slimecloud.slimeball.features.birthday;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import lombok.Getter;

@Getter
public class BirthdayConfig extends ConfigCategory {

	@ConfigField(name = "Announcement Kanal", command = "annouce-channel", description = "Der Chat in dem geburtstage angekündigt werden", type = ConfigFieldType.MESSAGE_CHANNEL)
	public long announceChat;

	@ConfigField(name = "Report Kanal", command = "report-channel", description = "Der chat in den eine warnung gesendet wird wenn ein nutzer unter 16 ist", type = ConfigFieldType.MESSAGE_CHANNEL)
	public long reportChat;

	@ConfigField(name = "Geburtstags-Rolle", command = "role", description = "Die rolle die an nutzer vergeben wird wenn sie geburtstag haben", type = ConfigFieldType.ROLE)
	public long birthdayRole;

}
