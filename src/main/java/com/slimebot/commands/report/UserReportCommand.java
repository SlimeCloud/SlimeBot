package com.slimebot.commands.report;

import com.slimebot.main.Main;
import com.slimebot.report.Report;
import com.slimebot.report.Type;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.events.interaction.ModalHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.Instant;

@ApplicationCommand(name = "Nutzer melden", type = Command.Type.USER, guildOnly = true)
public class UserReportCommand {
	public static Modal createMode(String targetUser) {
		return Modal.create("report:user", "Nutzer melden")
				.addActionRow(
						TextInput.create("user", "ID des Nutzers (NICHT ÄNDERN!)", TextInputStyle.SHORT)
								.setRequired(true)
								.setRequiredRange(17, 19) //Discord Snowflake IDs are 17 to 19 digits long
								.setPlaceholder("Snowflake ID des Nutzers")
								.setValue(targetUser)
								.build()
				)
				.addActionRow(
						TextInput.create("reason", "Warum möchtest du diese Person Reporten?", TextInputStyle.SHORT)
								.setMinLength(15)
								.setMaxLength(500)
								.setRequired(true)
								.setPlaceholder("Hier deine Begründung")
								.build()
				)
				.build();
	}

	@ApplicationCommandMethod
	public void performCommand(UserContextInteractionEvent event) {
		if(Main.blocklist(event.getGuild().getId()).contains(event.getMember().getId())) {
			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.embedColor(event.getGuild().getId()))
							.setTitle(":exclamation: Error: Blocked")
							.setDescription("Du wurdest gesperrt, so dass du keine Reports mehr erstellen kannst")
							.build()
			).setEphemeral(true).queue();
			return;
		}

		event.replyModal(createMode(event.getTarget().getId())).queue();
	}

	@WhenFinished
	public void setup(DiscordUtils manager) {
		manager.getEventManager().registerHandler(new ModalHandler("report:user", event -> {
			try {
				Main.jdaInstance.retrieveUserById(event.getValue("user").getAsString()).queue(user ->
								Report.createReport(event, id -> new Report(id, Type.USER, user, event.getUser(), event.getValue("reason").getAsString()), user),
						new ErrorHandler().handle(ErrorResponse.UNKNOWN_USER, e -> event.reply("Nutzer nicht gefunden").setEphemeral(true).queue())
				);
			} catch(NumberFormatException e) {
				event.reply("Ungültige Nutzer ID").setEphemeral(true).queue();
			}
		}));
	}
}
