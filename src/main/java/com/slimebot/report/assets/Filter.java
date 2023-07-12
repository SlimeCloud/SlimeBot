package com.slimebot.report.assets;

import java.util.function.Predicate;

public enum Filter {
	ALL(report -> true),
	CLOSED(report -> !report.isOpen()),
	OPEN(Report::isOpen);

	public final Predicate<Report> filter;

	Filter(Predicate<Report> filter) {
		this.filter = filter;
	}
}
