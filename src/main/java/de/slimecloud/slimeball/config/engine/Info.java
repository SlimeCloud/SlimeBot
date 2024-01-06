package de.slimecloud.slimeball.config.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Info {
	ConfigFieldType keyType() default ConfigFieldType.STRING;

	double minValue() default Double.MIN_VALUE;

	double maxValue() default Double.MAX_VALUE;
}
