package de.slimecloud.slimeball.features.birthday;

import de.cyklon.jevent.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.entities.Member;


@Data
@EqualsAndHashCode(callSuper = true)
public class BirthdayEvent extends Event {

	private final Member member;
	private final Birthday birthday;

}
