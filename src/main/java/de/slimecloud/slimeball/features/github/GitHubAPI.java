package de.slimecloud.slimeball.features.github;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.mineking.discordutils.restaction.CustomRestActionManager;
import de.mineking.discordutils.restaction.HttpHost;
import de.slimecloud.slimeball.main.Main;
import lombok.Getter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.function.Function;

public class GitHubAPI {
	private final static Route.CompiledRoute route = Route.post("graphql").compile();

	@Getter
	private final GitHub api;
	private final String token;

	private HttpHost host;

	public GitHubAPI(@NotNull String token) throws IOException {
		this.token = token;
		this.api = new GitHubBuilder().withOAuthToken(token).build();
	}

	public void init(@NotNull CustomRestActionManager manager) {
		host = manager.createHost("https://api.github.com/");
		host.getDefaultHeaders().put("Authorization", "Bearer " + token);

	}

	public <T> RestAction<T> execute(@NotNull String query, @Nullable Function<JsonObject, T> handler) {
		return host.request(route,
				(request, response) -> handler == null ? null : handler.apply(JsonParser.parseString(response.body().string()).getAsJsonObject().getAsJsonObject("data")),
				RequestBody.create(Main.json.toJson(new Query(query)), MediaType.get("text/json")),
				null
		);
	}

	private record Query(@NotNull String query) {
	}
}
