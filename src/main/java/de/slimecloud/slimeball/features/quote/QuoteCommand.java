package de.slimecloud.slimeball.features.quote;

import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.cooldown.Cooldown;
import de.mineking.discordutils.commands.condition.cooldown.CooldownIncrement;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.events.Listener;
import de.mineking.discordutils.events.handlers.ButtonHandler;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationCommand(name = "quote", description = "Zitiere jemanden", defer = true)
public class QuoteCommand {
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getQuoteChannel().isPresent();

	@Setup
	public static void setup(@NotNull DiscordUtils<?> manager, @NotNull SlimeBot bot) {
		manager.getJDA().addEventListener(new QuoteDeleteListener(bot));
	}

	@Cooldown(interval = 1, unit = TimeUnit.DAYS, uses = 2, auto = false, identifier = "quote")
	public void handleCooldown(@NotNull ICommandContext context) {
		context.getEvent().reply(":timer: :x: Du kannst nur 2 mal pro Tag zitieren!").setEphemeral(true).queue();
	}

	@ApplicationCommandMethod
	public static void quote(@NotNull SlimeBot bot, @NotNull IReplyCallback event,
	                         @Option(description = "Das Mitglied, das du zitieren möchtest") Member author,
	                         @Option(name = "message", description = "Die Aussage des Mitglieds") String content,
	                         @Nullable Message message,
	                         @Nullable TemporalAccessor timestamp,
	                         @CooldownIncrement Runnable cooldown
	) {
		if (author == null || author.getUser().isBot()) {
			event.getHook().sendMessage(":x: Du kannst diese Nachricht nicht zitieren!").setEphemeral(true).queue();
			return;
		}

		//Check author
		if (author.equals(event.getMember())) {
			event.getHook().editOriginal(":x: Du kannst dich nicht selbst zitieren!").queue();
			return;
		}

		bot.loadGuild(event.getGuild()).getQuoteChannel().ifPresent(channel -> {
			//Check channel
			if (channel.equals(event.getChannel())) {
				event.getHook().editOriginal(":x: Du kannst keine Zitate zitieren!").queue();
				return;
			}

			//Call event
			if (new UserQuotedEvent(event.getUser(), author, content, message).callEvent()) return;

			cooldown.run();

			//Send quotation
			channel.sendMessage(author.getAsMention()).addEmbeds(new EmbedBuilder()
					.setColor(bot.getColor(author.getGuild()))
					.setAuthor(author.getEffectiveName(), null, author.getEffectiveAvatarUrl())
					.setDescription(Arrays.stream(content.split("\n"))
							.map(s -> "> " + s)
							.collect(Collectors.joining("\n"))
					)
					.appendDescription("\n\n" + (message != null ? message.getJumpUrl() : "*Nicht verifiziert*"))
					.setImage(Optional.ofNullable(message)
							.flatMap(mes -> mes.getAttachments().stream()
									.filter(Message.Attachment::isImage)
									.map(Message.Attachment::getUrl)
									.findFirst()
							).orElse(null)
					)
					.setFooter("Zitiert von: " + event.getMember().getUser().getName())
					.setTimestamp(timestamp != null ? timestamp : Instant.now())
					.build()
			).addActionRow(
					Button.secondary("quote:guidance", "Wie zitiere ich?")
			).flatMap(mes -> event.getHook().editOriginal(author.getAsMention() + " zitiert")).queue();
		});
	}

	@Listener(type = ButtonHandler.class, filter = "quote:guidance")
	public void sendGuidance(@NotNull DiscordUtils<?> manager, @NotNull ButtonInteractionEvent event) {
		event.reply("""
				Du möchtest selbst ein Zitat wie dieses teilen? Kein Problem! Folge einfach den Folgenden Schritten:
				## Regeln
				1. Bitte zitiere nur Aussagen von diesem Server
				2. Zitiere nur besondere Nachrichten. Ein "*Hi*" ist nicht unbedingt zitat würdig
				3. Stelle sicher, dass du nur Mitglieder zitierst, wenn die zitierte Person nichts dagegen hat. Respektiere bitte das Urheber recht und die Privatsphäre!
				                
				## Zitat löschen
				Wenn du zitiert wurdest, aber dieses Zitat gerne entfernen möchtest, kannst du die `❌`-Reaktion zur Nachricht in diesem Kanal hinzufügen, um diese zu löschen.
				                
				## Selbst Jemanden zitieren
				Es gibt zwei Möglichkeiten, um ein Zitat zu senden. Bitte beachte dabei die oben genannten Regeln!
				                
				### 1. Context-Befehl
				Du kannst einfach eine Nachricht Rechtsklicken, "Apps" und dann "Nachricht Zitieren" auswählen. Die Nachricht wird anschließend vollautomatisch in den Zitate Kanal weitergeleitet.
				### 2. Slash-Befehl
				Wenn die Aussage in einem Sprachchat stattgefunden hat, kannst du auch %command verwenden. Bitte versuche jedoch, den Wortlaut möglichst genau am Original zu halten.
				Bei dieser Methode bist du selbst für die richtigkeit des Zitats verantwortlich!
				""".replace("%command%", manager.getCommandManager().getCommand(QuoteCommand.class).getAsMention(event.getGuild().getIdLong()))
		).setEphemeral(true).queue();
	}
}
