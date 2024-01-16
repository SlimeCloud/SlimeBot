package de.slimecloud.slimeball.features.birthday.event;

import de.slimecloud.slimeball.features.birthday.Birthday;
import net.dv8tion.jda.api.entities.Member;

public class BirthdayEndEvent extends BirthdayEvent {

	public BirthdayEndEvent(Birthday birthday) {
		super(birthday);
	}

	public BirthdayEndEvent(Member member) {
		super(member);
	}
}
