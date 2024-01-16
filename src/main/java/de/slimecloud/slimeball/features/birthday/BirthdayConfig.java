package de.slimecloud.slimeball.features.birthday;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
public class BirthdayConfig extends ConfigCategory {

	@ConfigField(name = "Report Kanal", command = "report-channel", description = "Der chat in den eine warnung gesendet wird wenn ein nutzer unter 16 ist", type = ConfigFieldType.MESSAGE_CHANNEL)
	public Long reportChat;

	@ConfigField(name = "Geburtstags-Rolle", command = "role", description = "Die rolle die an nutzer vergeben wird wenn sie geburtstag haben", type = ConfigFieldType.ROLE)
	public Long birthdayRole;

	@NotNull
	public Optional<MessageChannel> getReportChat() {
		return Optional.ofNullable(reportChat).map(id -> bot.getJda().getChannelById(MessageChannel.class, id));
	}

	@NotNull
	public Optional<Role> getBirthdayRole() {
		return Optional.ofNullable(birthdayRole).map(bot.getJda()::getRoleById);
	}
}
