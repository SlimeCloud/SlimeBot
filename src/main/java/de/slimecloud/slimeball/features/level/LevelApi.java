package de.slimecloud.slimeball.features.level;

import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.main.api.ErrorResponse;
import de.slimecloud.slimeball.main.api.ErrorResponseType;
import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.*;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class LevelApi implements EndpointGroup {
	private final SlimeBot bot;

	@Override
	public void addEndpoints() {
		get("/{guild}/{user}", new GetLevelEndpoint());
		post("/{guild}/{user}", new AddLevelEndpoint());
	}

	private class GetLevelEndpoint implements Handler {
		@Override
		public void handle(@NotNull Context ctx) {
			Long guildId = ctx.pathParamAsClass("guild", Long.class).get();
			Long userId = ctx.pathParamAsClass("user", Long.class).get();

			Guild guild = bot.getJda().getGuildById(guildId);
			if (guild == null) throw new ErrorResponse(ErrorResponseType.GUILD_NOT_FOUND);

			Member member = guild.getMemberById(userId);
			if (member == null) throw new ErrorResponse(ErrorResponseType.MEMBER_NOT_FOUND);

			ctx.json(bot.getLevel().getLevel(member));
		}
	}

	private class AddLevelEndpoint implements Handler {
		private record Request(Integer level, Integer xp) {}

		@Override
		public void handle(@NotNull Context ctx) {
			Long guildId = ctx.pathParamAsClass("guild", Long.class).get();
			Long userId = ctx.pathParamAsClass("user", Long.class).get();

			Guild guild = bot.getJda().getGuildById(guildId);
			if (guild == null) throw new ErrorResponse(ErrorResponseType.GUILD_NOT_FOUND);

			Member member = guild.getMemberById(userId);
			if (member == null) throw new ErrorResponse(ErrorResponseType.MEMBER_NOT_FOUND);

			Request request = ctx.bodyAsClass(Request.class);

			if (request.level != null) bot.getLevel().addLevel(member, request.level);
			if (request.xp != null) bot.getLevel().addXp(member, request.xp, UserGainXPEvent.Type.MANUAL);

			ctx.json(bot.getLevel().getLevel(member));
		}
	}
}
