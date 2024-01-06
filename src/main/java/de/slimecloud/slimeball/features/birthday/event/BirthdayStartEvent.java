package de.slimecloud.slimeball.features.birthday.event;

import de.slimecloud.slimeball.features.birthday.Birthday;
import lombok.Getter;

@Getter
public class BirthdayStartEvent extends BirthdayEvent  {

	private final Birthday birthday;
	public BirthdayStartEvent(Birthday birthday) {
		super(birthday);
		this.birthday = birthday;
	}
}
