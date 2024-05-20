package de.slimecloud.slimeball.features.alerts.holiday.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Country implements Nameable {
	private String isoCode;
	private Name[] name;
}
