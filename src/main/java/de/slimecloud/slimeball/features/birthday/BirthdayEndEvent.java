package de.slimecloud.slimeball.features.birthday;

import net.dv8tion.jda.api.entities.Member;

public class BirthdayEndEvent extends BirthdayEvent {
	public BirthdayEndEvent(Member member, Birthday birthday) {
		super(member, birthday);
	}
}
