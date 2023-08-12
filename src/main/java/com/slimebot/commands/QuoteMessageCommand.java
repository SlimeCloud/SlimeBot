package com.slimebot.commands;

import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.QuoteConfig;
import de.mineking.discord.Utils;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.events.Listener;
import de.mineking.discord.events.interaction.ButtonHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;

@ApplicationCommand(name = "Nachricht Zitieren", type = Command.Type.MESSAGE, feature = "quote")
public class QuoteMessageCommand {
	@ApplicationCommandMethod
	public void performCommand(MessageContextInteractionEvent event) {
		if(event.getTarget().getAuthor().equals(event.getUser())) {
			event.reply("Nachricht zitiert").setEphemeral(true).queue();
			quote(event.getTarget());
			return;
		}

		event.deferReply(true).queue();

		event.getTarget().getAuthor().openPrivateChannel()
				.flatMap(channel -> channel
						.sendMessage(event.getUser().getAsMention() + " möchte die folgende Nachricht von dir zitieren: " + event.getTarget().getJumpUrl())
						.addActionRow(
								Button.success("quote:accept:" + event.getTarget().getJumpUrl().replace("https://discord.com/channels/", ""), "Annehmen"),
								Button.danger("quote:deny", "Ablehnen")
						)
				)
				.queue(
						mes -> event.getHook().editOriginal("Anfrage gesendet! Der Autor der Nachricht muss bestätigen, dass er zitiert werden möchte").queue(),
						new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
								e -> event.getHook().editOriginal("Es konnte keine Anfrage an den Autor gesendet werden!").queue()
						)
				);
	}

	public void quote(Message message) {
		GuildConfig.getConfig(message.getGuild()).getQuoteConfig().flatMap(QuoteConfig::getChannel).ifPresent(channel ->
				channel.sendMessageEmbeds(
						new EmbedBuilder()
								.setColor(GuildConfig.getColor(message.getGuild()))
								.setAuthor(message.getMember().getEffectiveName(), null, message.getMember().getEffectiveAvatarUrl())
								.setDescription(">>> " + message.getContentRaw())
								.setTimestamp(message.getTimeCreated())
								.build()
				).queue()
		);
	}

	@Listener(type = ButtonHandler.class, filter = "quote:deny")
	public void handleDeny(ButtonInteractionEvent event) {
		event.getMessage().delete().queue();
		event.reply("Anfrage abgelehnt").setEphemeral(true).queue();
	}

	@Listener(type = ButtonHandler.class, filter = "quote:accept:(.*)")
	public void handleAccept(ButtonInteractionEvent event) {
		event.getMessage().delete().queue();
		event.reply("Anfrage angenommen").setEphemeral(true).queue();

		Utils.getMessageByJumpUrl("https://discord.com/channels/" + event.getComponentId().split(":", 3)[2], event.getJDA()).queue(this::quote);
	}
}
