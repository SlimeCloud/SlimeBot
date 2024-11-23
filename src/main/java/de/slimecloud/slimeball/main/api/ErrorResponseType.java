package de.slimecloud.slimeball.main.api;

import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ErrorResponseType {
	UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST),

	GUILD_NOT_FOUND(HttpStatus.NOT_FOUND),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND);

	public final HttpStatus status;
}
