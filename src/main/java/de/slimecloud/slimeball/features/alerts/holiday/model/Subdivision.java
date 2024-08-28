package de.slimecloud.slimeball.features.alerts.holiday.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Subdivision implements Nameable {
	private String code;
	private Name[] name;
}
