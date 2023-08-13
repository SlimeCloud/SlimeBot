package com.slimebot.commands;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.QuoteConfig;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import de.mineking.discord.events.Listener;
import de.mineking.discord.events.interaction.ButtonHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.stream.Collectors;

@ApplicationCommand(name = "quote", description = "Zitiere jemanden", feature = "quote")
public class QuoteCommand {
	@ApplicationCommandMethod
	public static void quote(IReplyCallback event,
	                  @Option(description = "Das Mitglied, das du zitieren möchtest") Member author,
	                  @Option(description = "Die Aussage des Mitglieds") String message,
	                  String url,
	                  TemporalAccessor timestamp
	) {
		event.deferReply(true).queue();

		GuildConfig.getConfig(author.getGuild()).getQuoteConfig().flatMap(QuoteConfig::getChannel).ifPresent(channel ->
				channel.sendMessageEmbeds(
						new EmbedBuilder()
								.setColor(GuildConfig.getColor(author.getGuild()))
								.setAuthor(author.getEffectiveName(), null, author.getEffectiveAvatarUrl())
								.setDescription(
										Arrays.stream(message.split("\n"))
												.map(s -> "> " + s)
												.collect(Collectors.joining("\n"))
								)
								.appendDescription(url != null
										? "\n\n" + url
										: ""
								)
								.setFooter("Zitiert von: " + event.getMember().getEffectiveName())
								.setTimestamp(timestamp != null ? timestamp : Instant.now())
								.build()
				).addActionRow(
						Button.secondary("quote:guidance", "Wie zitiere ich?")
				).flatMap(mes -> event.getHook().editOriginal(author.getAsMention() + " zitiert")).queue()
		);
	}

	@Listener(type = ButtonHandler.class, filter = "quote:guidance")
	public void sendGuidance(ButtonInteractionEvent event) {
		event.reply("""
                Du möchtest selbst ein Zitat wie dieses teilen? Kein Problem! Folge einfach den Folgenden Schritten:
                ## Regeln
                1. Bitte zitiere nur Aussagen von diesem Server
                2. Zitiere nur besondere Nachrichten. Ein "*Hi*" ist nicht unbedingt zitat würdig
                3. Stelle sicher, dass du nur Mitglieder zitierst, wenn die zitierte Person nichts dagegen hat. Respektiere bitte das Urheber recht und die Privatsphäre!
                
                ## Selbst jemanden zitieren
                Es gibt zwei Möglichkeiten, um ein Zitat zu senden. Bitte beachte dabei die oben genannten Regeln!
                
                ### Context-Befehl
                Du kannst einfach eine Nachricht Rechtsklicken, "Apps" und dann "Nachricht Zitieren" auswählen. Die Nachricht wird anschließend vollautomatisch in den Zitate Kanal weitergeleitet.
                ### Slash-Befehl
                Wenn die Aussage in einem Sprachchat stattgefunden hat, kannst du auch </quote:%commandid%> verwenden. Bitte versuche jedoch, den Wortlaut möglichst genau am Original zu halten.
                Bei dieser Methode bist du selbst für die richtigkeit des Zitats verantwortlich!
                """.replace("%commandid%", String.valueOf(Main.discordUtils.getCommandCache().getGuildCommand(event.getGuild().getIdLong(), "quote")))
		).setEphemeral(true).queue();
	}
}
