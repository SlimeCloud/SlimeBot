package de.slimecloud.slimeball.config.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigField {
	String name();

	String command();

	String description();

	ConfigFieldType type();

	boolean required() default false;
}
