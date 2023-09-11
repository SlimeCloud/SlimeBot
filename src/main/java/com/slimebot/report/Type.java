package com.slimebot.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Type {
    MESSAGE("Nachricht"),
    USER("User");

    private final String str;
}

