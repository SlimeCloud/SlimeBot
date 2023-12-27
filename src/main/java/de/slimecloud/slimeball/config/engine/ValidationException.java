package de.slimecloud.slimeball.config.engine;

public class ValidationException extends RuntimeException {
	public ValidationException(Exception e) {
		super(e);
	}
}
