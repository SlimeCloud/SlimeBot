package de.slimecloud.slimeball.features.message;

import de.slimecloud.slimeball.main.SlimeBot;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AutoMessageConfig {
	public transient SlimeBot bot;

	public record MessageInfo(long channel, long message, String name) {}

	private List<MessageInfo> messages = new ArrayList<>();

	public String loadMessage(@NotNull Guild guild, @NotNull String message) {
		return bot.loadGuildResource(guild, "messages/" + message, false, file -> Files.readString(file.toPath(), StandardCharsets.UTF_8)).orElse("*Nachricht nicht gefunden*");
	}

	public RestAction<?> sendMessage(@NotNull GuildMessageChannel channel, @NotNull String message) {
		String msg = loadMessage(channel.getGuild(), message);
		return channel.sendMessage(msg).onSuccess(mes -> messages.add(new MessageInfo(channel.getIdLong(), mes.getIdLong(), message)));
	}

	public RestAction<?> update(@NotNull Guild guild) {
		return RestAction.allOf(new ArrayList<>(messages).stream()
				.map(mes -> {
					MessageChannel channel = guild.getChannelById(GuildMessageChannel.class, mes.channel);

					if (channel == null) {
						messages.remove(mes);
						return bot.wrap(null);
					}

					return channel.editMessageById(mes.message, loadMessage(guild, mes.name)).onErrorMap(exception -> {
						if (exception instanceof ErrorResponseException e) {
							if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) messages.remove(mes);
						}

						return null;
					});
				})
				.toList()
		);
	}
}
