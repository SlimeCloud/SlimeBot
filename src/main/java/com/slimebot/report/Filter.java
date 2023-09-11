package com.slimebot.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum Filter {
    ALL(report -> true),
    CLOSED(report -> !report.isOpen()),
    OPEN(Report::isOpen);

    private final Predicate<Report> filter;
}
