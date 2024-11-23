package de.slimecloud.slimeball.main.api;

import com.google.gson.JsonSyntaxException;
import de.slimecloud.slimeball.features.level.LevelApi;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.json.JsonMapper;
import io.javalin.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

import static io.javalin.apibuilder.ApiBuilder.path;

@Slf4j
public class Server {
	public Server(SlimeBot bot) {
		Javalin server = Javalin.create(javalin -> {
			javalin.http.defaultContentType = "text/json";
			javalin.useVirtualThreads = true;
			javalin.showJavalinBanner = false;

			javalin.jsonMapper(new JsonMapper() {
				@NotNull
				@Override
				public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
					return Main.json.fromJson(json, targetType);
				}

				@NotNull
				@Override
				public String toJsonString(@NotNull Object obj, @NotNull Type type) {
					return Main.json.toJson(obj, type);
				}
			});

			javalin.router.apiBuilder(() -> {
				path("/level", new LevelApi(bot));
			});
		});

		server.exception(JsonSyntaxException.class, (e, ctx) ->  { throw new ErrorResponse(ErrorResponseType.INVALID_REQUEST); });
		server.exception(ValidationException.class, (e, ctx) -> { throw new ErrorResponse(ErrorResponseType.INVALID_REQUEST); });

		server.exception(Exception.class, (e, ctx) -> {
			logger.error("Error in http handler", e);
			throw new InternalServerErrorResponse();
		});

		server.exception(HttpResponseException.class, (e, ctx) -> { throw new ErrorResponse(ErrorResponseType.UNKNOWN, HttpStatus.forStatus(e.getStatus())); });

		server.exception(ErrorResponse.class, (e, ctx) -> {
			try {
				ctx.status(e.statusCode).json(e.data());
			} catch (Exception ex) {
				logger.error("Error sending response", ex);
				ctx.status(500).json(new ErrorResponse(ErrorResponseType.UNKNOWN).data());
			}
		});

		server.start(bot.getConfig().getPort());
	}
}
