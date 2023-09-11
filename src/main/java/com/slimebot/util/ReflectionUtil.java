package com.slimebot.util;

import java.util.stream.Stream;

public final class ReflectionUtil {

    public static Stream<Class<?>> getDeclaredClasses(Class<?> clazz) {
        return Stream.concat(
                Stream.of(clazz),
                Stream.of(clazz.getDeclaredClasses())
                        .flatMap(ReflectionUtil::getDeclaredClasses)
        );
    }

}
