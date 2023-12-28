package de.slimecloud.slimeball.features.report.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.events.Listener;
import de.mineking.discordutils.events.handlers.ModalHandler;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@ApplicationCommand(name = "Nutzer melden", type = Command.Type.USER, scope = Scope.GUILD_GLOBAL)
public class UserReportCommand {
	@NotNull
	public static Modal createMode(@NotNull String targetUser) {
		return Modal.create("report:user:" + targetUser, "Nutzer melden")
				.addActionRow(TextInput.create("reason", "Warum möchtest du diese Person Reporten?", TextInputStyle.SHORT)
						.setPlaceholder("Hier deine Begründung")
						.setMinLength(15)
						.setMaxLength(500)
						.setRequired(true)
						.build()
				)
				.build();
	}

	public static void submitUserReport(@NotNull SlimeBot bot, @NotNull IReplyCallback event, @NotNull User target, @NotNull String reason) {
		//Try report and check for successful insertion
		if (!bot.getReports().reportUser(event, target, reason)) return;

		event.replyEmbeds(new EmbedBuilder()
				.setTitle(":white_check_mark: Report Erfolgreich")
				.setColor(bot.getColor(event.getGuild()))
				.setDescription(target.getAsMention() + " wurde erfolgreich gemeldet")
				.setTimestamp(Instant.now())
				.build()
		).setEphemeral(true).queue();
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull UserContextInteractionEvent event) {
		event.replyModal(createMode(event.getTarget().getId())).queue();
	}

	@Listener(type = ModalHandler.class, filter = "report:user:(\\d+)")
	public void handleReportModal(@NotNull SlimeBot bot, @NotNull ModalInteractionEvent event) {
		//Retrieve (will use cache if present)
		event.getJDA().retrieveUserById(event.getModalId().split(":")[2]).queue(
				user -> submitUserReport(bot, event, user, event.getValue("reason").getAsString()),
				new ErrorHandler().handle(ErrorResponse.UNKNOWN_USER, e -> event.reply("Nutzer nicht gefunden").setEphemeral(true).queue())
		);
	}
}
