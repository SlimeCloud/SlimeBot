package com.slimebot.commands;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;

public class Bug extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(!event.getFullCommandName().equals("bug")) return;

		TextInput textInput = TextInput.create("bug:" + event.getInteraction().getMember().getId(), "Bug", TextInputStyle.PARAGRAPH)
				.setMinLength(10)
				.build();

		Modal modal = Modal.create("bug", "Melde einen Bug")
				.addActionRow(textInput)
				.build();

		event.replyModal(modal).queue();
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		if(!event.getModalId().equals("bug")) return;

		YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");

		try {
			config.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		ModalMapping modalMapping = event.getValues().get(0);

		String label = "Ein neuer Bug wurde gefunden!";

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setColor(Main.embedColor(event.getGuild().getId()))
				.setTitle(label)

				.setDescription("Fehlerbeschreibung: \n\n")
				.appendDescription(modalMapping.getAsString() + "\n")
				.setFooter("Report von: " + event.getUser().getGlobalName() + " (" + event.getUser().getId() + ")");

		event.reply("Der Report wurde erfolgreich ausgeführt").setEphemeral(true).queue();

		event.getGuild()
				.getTextChannelById(config.getString("logChannel"))
				.sendMessageEmbeds(embedBuilder.build())
				.setActionRow(Button.secondary("close_bug", "Bug schließen")).queue();
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if(!event.getButton().getId().equals("close_bug")) return;

		event.getMessage().delete().queue();
		event.reply("Der Bug wurde erfolgreich geschlossen!").setEphemeral(true).queue();
	}
}
