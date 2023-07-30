package com.slimebot.commands.config.engine;

import java.awt.*;
import java.util.function.Predicate;

public enum FieldVerification {
	ALL(x -> true),
	COLOR(x -> {
		if (!(x instanceof String s)) return false;

		try {
			Color.decode(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	});

	public final Predicate<Object> verifier;

	FieldVerification(Predicate<Object> verifier) {
		this.verifier = verifier;
	}
}