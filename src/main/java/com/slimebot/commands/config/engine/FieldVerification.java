package com.slimebot.commands.config.engine;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
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

    private final Predicate<Object> verifier;
}