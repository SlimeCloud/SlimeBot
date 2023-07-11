package com.slimebot.commands;

import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import com.slimebot.main.SlimeEmoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class Fdmds extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(!event.getFullCommandName().equals("fdmds")) return;

		Modal modal = getFdmdsModal("fdmds", null);
		event.replyModal(modal).queue();
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		switch(event.getModalId()) {
			case "fdmds" -> {
				// get Log-channel
				MessageChannel channel = Main.database.getChannel(event.getGuild(), DatabaseField.FDMDS_LOG_CHANNEL);

				if(channel == null) {
					event.reply("Error: Channel wurde nicht gesetzt!").setEphemeral(true).queue();
					return;
				}

				// Get Contents
				String question = event.getValue("fdmds.question").getAsString();
				String[] choices = event.getValue("fdmds.choices").getAsString().split(";");

				if(choices.length <= 1) {
					event.reply("Du musst mindestens 2 Antwortmöglichkeiten angeben!").setEphemeral(true).queue();
					return;
				}
				if(choices.length > 9) {
					event.reply("Du kannst maximal 9 Antwortmöglichkeiten angeben!").setEphemeral(true).queue();
					return;
				}

				StringBuilder choicesStr = new StringBuilder();
				for(int i = 0; i < choices.length; i++) {
					choicesStr
							.append(SlimeEmoji.fromId(i).format())
							.append(" -> ")
							.append(choices[i].strip())
							.append("\r\n");
				}

				// Create Buttons
				Button editButton = Button.secondary("fdmds.editButton", "Edit");
				Button sendButton = Button.danger("fdmds.sendButton", "Senden");

				// Create and send Embed
				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setColor(Main.database.getColor(event.getGuild()))
						.setTitle("Frag doch mal den Schleim")
						.setFooter("Vorschlag von: " + event.getUser().getGlobalName() + " (" + event.getUser().getId() + ")")
						.addField("Frage:", "Heute würde ich gerne von euch wissen, " + question, false)
						.addField("Auswahlmöglichkeiten:", choicesStr.toString(), false);

				channel.sendMessageEmbeds(embedBuilder.build())
						.addActionRow(editButton, sendButton).queue();

				// Send User Feedback
				event.reply("Vorschlag erfolgreich verschickt!").setEphemeral(true).queue();
			}

			case "fdmds.edit" -> {
				// Get Contents
				String question = event.getValue("fdmds.edit.question").getAsString();
				String choices = event.getValue("fdmds.edit.choices").getAsString();

				EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0))
						.clearFields()
						.addField("Frage:", question, false)
						.addField("Auswahlmöglichkeiten:", choices, false);

				event.getMessage().editMessage("Edited").setEmbeds(embedBuilder.build()).queue();
				event.reply("Frage wurde bearbeitet.").setEphemeral(true).queue();
			}
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if(!event.getButton().getId().equals("fdmds.editButton") && !event.getButton().getId().equals("fdmds.sendButton")) return;

		switch(event.getButton().getId()) {
			case "fdmds.editButton" -> {
				// Get Contents
				MessageEmbed embed = event.getMessage().getEmbeds().get(0);
				String question = embed.getFields().get(0).getValue();
				String choices = embed.getFields().get(1).getValue();

				// Crate Edit Modal
				Modal modal = getFdmdsModal("fdmds.edit", new String[]{question, choices});
				event.replyModal(modal).queue();
			}

			case "fdmds.sendButton" -> {
				// create text
				String text = "Einen Wunderschönen <:slimewave:1080225151104331817> ,\r\n";

				MessageEmbed embed = event.getMessage().getEmbeds().get(0);
				String question = embed.getFields().get(0).getValue();
				String choices = embed.getFields().get(1).getValue();

				Role role = Main.database.getRole(event.getGuild(), DatabaseField.FDMDS_ROLE);

				if(role == null) {
					event.reply("Error: Rolle wurde nicht gesetzt!").setEphemeral(true).queue();
					return;
				}

				text = text + " \r\n" + question + "\r\n \r\n" + choices + "\n\n" + role.getAsMention();

				// get fdmds-channel
				MessageChannel channel = Main.database.getChannel(event.getGuild(), DatabaseField.FDMDS_CHANNEL);

				if(channel == null) {
					event.reply("Error: Channel wurde nicht gesetzt!").setEphemeral(true).queue();
					return;
				}

				// Send and add reactions
				channel.sendMessage(text).queue(m -> {
					for(int i = 0; i < choices.lines().count(); i++) {
						m.addReaction(SlimeEmoji.fromId(i).emoji).queue();
					}

					event.reply("Frage verschickt!").setEphemeral(true).queue();
				});
				event.getMessage().delete().queue();
			}
		}
	}

	// idPrefix must be 'fdmds' or 'fdmds.edit'
	// value is only set if it is the edit Modal
	private Modal getFdmdsModal(String idPrefix, String[] values) {
		if(idPrefix == null) return null;

		TextInput.Builder questionTextInput = TextInput.create(idPrefix + ".question", "Deine Frage", TextInputStyle.SHORT)
				.setMinLength(10)
				.setMaxLength(150)
				.setRequired(true);
		if(values == null) questionTextInput.setPlaceholder("Welche Eissorte mögt ihr am liebsten?");
		if(values != null) questionTextInput.setValue(values[0]);

		TextInput.Builder choicesTextInput = TextInput.create(idPrefix + ".choices", "Deine Antwortmöglichkeiten", TextInputStyle.PARAGRAPH)
				.setMinLength(10)
				.setMaxLength(800)
				.setRequired(true);
		if(values == null) choicesTextInput.setPlaceholder("Antworten mit ; trennen z.B. Erdbeere; Cookie; Schokolade");
		if(values != null) choicesTextInput.setValue(values[1]);

		return Modal.create(idPrefix, "Schlage eine fdmds Frage vor")
				.addActionRow(questionTextInput.build())
				.addActionRow(choicesTextInput.build())
				.build();
	}
}
