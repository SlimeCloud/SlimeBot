package de.slimecloud.slimeball.features.alerts.holiday.model;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public interface Nameable {
	@NotNull
	Name[] getName();

	@NotNull
	default String getName(@NotNull String language) {
		return Arrays.stream(getName())
				.filter(n -> n.getLanguage().equals(language))
				.findFirst().map(Name::getText).orElseThrow();
	}
}
