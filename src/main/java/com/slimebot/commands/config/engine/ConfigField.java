package com.slimebot.commands.config.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigField {
	String command();
	String title() default "";
	String description();
	ConfigFieldType type();
	FieldVerification verifier() default FieldVerification.ALL;
}
