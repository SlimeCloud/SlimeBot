package de.slimecloud.slimeball.features.alerts.holiday.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SchoolHoliday implements Nameable {
	private String startDate;
	private String endDate;

	private Name[] name;
	private Subdivision[] subdivisions;

	@Getter
	@ToString
	public static class Subdivision {
		private String code;
	}
}
