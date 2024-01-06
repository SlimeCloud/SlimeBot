package de.slimecloud.slimeball.features.birthday.event;

import de.cyklon.jevent.Event;
import de.slimecloud.slimeball.features.birthday.Birthday;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;


@Data
@EqualsAndHashCode(callSuper = true)
public class BirthdayEvent extends Event {

	private final Member member;
	private final Guild guild;

	protected BirthdayEvent(Birthday birthday) {
		this(birthday.getGuild().getMember(birthday.getUser()));
	}

	protected BirthdayEvent(Member member) {
		this.member = member;
		this.guild = member.getGuild();
	}
}
