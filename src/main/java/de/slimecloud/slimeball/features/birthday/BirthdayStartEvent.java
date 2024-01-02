package de.slimecloud.slimeball.features.birthday;

import net.dv8tion.jda.api.entities.Member;

public class BirthdayStartEvent extends BirthdayEvent  {
	public BirthdayStartEvent(Member member, Birthday birthday) {
		super(member, birthday);
	}
}
