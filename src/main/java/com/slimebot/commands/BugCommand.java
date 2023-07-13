package com.slimebot.commands;

import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.events.interaction.ButtonHandler;
import de.mineking.discord.events.interaction.ModalHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

@ApplicationCommand(name = "bug", description = "Melde einen Bug")
public class BugCommand {
	public final static Modal modal = Modal.create("bug", "Melde einen Bug")
			.addActionRow(
					TextInput.create("text", "Bug", TextInputStyle.PARAGRAPH)
							.setMinLength(10)
							.build()
			)
			.build();

	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event) {
		event.replyModal(modal).queue();
	}

	@WhenFinished
	public void setup(DiscordUtils manager) {
		manager.getEventManager().registerHandler(new ModalHandler("bug", event -> {
			event.reply("Der Report wurde erfolgreich ausgeführt").setEphemeral(true).queue();

			Main.database.getChannel(event.getGuild(), DatabaseField.LOG_CHANNEL)
					.sendMessageEmbeds(
							new EmbedBuilder()
									.setColor(Main.database.getColor(event.getGuild()))
									.setTitle("Ein neuer Bug wurde gefunden!")

									.setDescription("Fehlerbeschreibung: \n\n")
									.appendDescription(event.getValue("text").getAsString() + "\n")
									.setFooter("Report von: " + event.getUser().getGlobalName() + " (" + event.getUser().getId() + ")")
									.build()
					)
					.setActionRow(Button.secondary("bug:close", "Bug schließen")).queue();
		}));

		manager.getEventManager().registerHandler(new ButtonHandler("bug:close", event -> {
			event.getMessage().delete().queue();
			event.reply("Der Bug wurde erfolgreich geschlossen!").setEphemeral(true).queue();
		}));
	}
}
