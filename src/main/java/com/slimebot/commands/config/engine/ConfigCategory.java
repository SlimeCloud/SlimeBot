package com.slimebot.commands.config.engine;

import com.slimebot.commands.config.setup.engine.CustomSetupFrame;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ConfigCategory {
	String name();

	String description();

	Class<?>[] subcommands() default {};

	boolean updateCommands() default false;

	Class<? extends CustomSetupFrame>[] customFrames() default {};
}
