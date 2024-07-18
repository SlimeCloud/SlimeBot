package de.slimecloud.slimeball.features.staff.absence;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Setter
@Getter
public class AbsenceConfig extends ConfigCategory {


	@ConfigField(name = "Abwesenheits-Rolle", command = "role", description = "Die rolle für Abwesende Teammembers", type = ConfigFieldType.ROLE, required = true)
	private Long absenceRole;

	@ConfigField(name = "Abwesenheits-Channel", command = "channel", description = "Der Channel zur Ankündigung abwesender Teammembers", type = ConfigFieldType.MESSAGE_CHANNEL)
	private Long absenceChannel;

	@NotNull
	public Optional<GuildMessageChannel> getChannel() {
		return Optional.ofNullable(bot.getJda().getChannelById(GuildMessageChannel.class, absenceChannel));
	}

	@NotNull
	public Optional<Role> getRole() {
		return Optional.ofNullable(bot.getJda().getRoleById(absenceRole));
	}
}
