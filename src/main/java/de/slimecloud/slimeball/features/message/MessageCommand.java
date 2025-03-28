package de.slimecloud.slimeball.features.message;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Autocomplete;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@ApplicationCommand(name = "message", description = "Verwaltet nachrichten, die vom Bot gesendet werden", scope = Scope.GUILD)
public class MessageCommand {
	public final CommandPermission permission = CommandPermission.TEAM;
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getAutoMessage().isPresent();

	public static void updateMessage(@NotNull SlimeBot bot, @NotNull Guild guild) {
		GuildConfig config = bot.loadGuild(guild);
		config.getAutoMessage().ifPresent(am -> am.update(guild).queue(x -> config.save()));
	}

	@ApplicationCommand(name = "send", description = "Sendet eine gespeicherte Nachricht als Bot")
	public static class SendCommand {
		@Autocomplete("message")
		public void autocomplete(@NotNull SlimeBot bot, @NotNull CommandAutoCompleteInteractionEvent event) {
			event.replyChoices(bot.loadGuildResource(event.getGuild(), "messages", false, File::list)
					.map(list -> Arrays.stream(list)
							.filter(e -> e.startsWith(event.getFocusedOption().getValue()))
							.map(e -> new Command.Choice(e, e))
							.toList()
					)
					.orElse(Collections.emptyList())
			).queue();
		}

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                               @Option(description = "Der Name der Datei") String message
		) {
			GuildConfig config = bot.loadGuild(event.getGuild());
			config.getAutoMessage().ifPresent(am -> am.sendMessage(event.getChannel().asGuildMessageChannel(), message).queue(x -> config.save()));

			event.deferReply(true).flatMap(InteractionHook::deleteOriginal).queue();
		}
	}

	@ApplicationCommand(name = "update", description = "Updated alle Nachrichten")
	public static class UpdateCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
			updateMessage(bot, event.getGuild());
			event.deferReply(true).flatMap(InteractionHook::deleteOriginal).queue();
		}
	}
}
