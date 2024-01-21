package de.slimecloud.slimeball.features.birthday;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
public class BirthdayConfig extends ConfigCategory {

	@ConfigField(name = "Geburtstags-Rolle", command = "role", description = "Die rolle die an nutzer vergeben wird wenn sie geburtstag haben", type = ConfigFieldType.ROLE)
	public Long birthdayRole;

	@NotNull
	public Optional<Role> getBirthdayRole() {
		return Optional.ofNullable(birthdayRole).map(bot.getJda()::getRoleById);
	}
}
