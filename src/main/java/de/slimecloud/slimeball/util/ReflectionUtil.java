package de.slimecloud.slimeball.util;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public final class ReflectionUtil {
	@NotNull
	public static Stream<Class<?>> getDeclaredClasses(@NotNull Class<?> clazz) {
		return Stream.concat(
				Stream.of(clazz),
				Stream.of(clazz.getDeclaredClasses()).flatMap(ReflectionUtil::getDeclaredClasses)
		);
	}
}
