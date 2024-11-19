package de.slimecloud.slimeball.main.api;

import io.javalin.http.HttpStatus;

public class ErrorResponse extends RuntimeException {
	public final ErrorResponseType type;
	public final int statusCode;
	public final String statusMessage;

	public ErrorResponse(ErrorResponseType type, HttpStatus status) {
		this.type = type;
		this.statusCode = status.getCode();
		this.statusMessage = status.getMessage();
	}

	public ErrorResponse(ErrorResponseType type) {
		this(type, type.status);
	}

	public Data data() {
		return new Data(type, statusCode, statusMessage);
	}

	public record Data(ErrorResponseType type, int statusCode, String statusMessage) {}
}
