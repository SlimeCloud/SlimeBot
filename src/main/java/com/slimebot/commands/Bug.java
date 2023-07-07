package com.slimebot.commands;

import com.slimebot.utils.Config;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
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
        super.onSlashCommandInteraction(event);

        if (event.getName().equalsIgnoreCase("bug")) {
            TextInput textInput = TextInput
                    .create("bug:" + event.getInteraction().getMember().getId(), "Bug", TextInputStyle.PARAGRAPH)
                    .setMinLength(10)
                    .build();
            textInput.isRequired();

            Modal modal = Modal
                    .create("bug" + event.getInteraction().getMember().getId(), "Melde einen Bug")
                    .addActionRow(textInput)
                    .build();
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        super.onModalInteraction(event);
        YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!(event.getModalId().equals("bug"))){return;}

        ModalMapping modalMapping = event.getInteraction().getValues().get(0);
        String label = "Ein neuer Bug wurde gefunden!";
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Main.embedColor(event.getGuild().getId()));
        embedBuilder.setTitle(label);

        if (modalMapping.getId().contains("bug")) {
            User user = Main.jdaInstance.retrieveUserById(modalMapping.getId().split(":")[1]).complete();
            embedBuilder.setDescription("Fehlerbeschreibung: \n\n");
            embedBuilder.appendDescription(modalMapping.getAsString() + "\n");
            embedBuilder.setFooter("Report von: " + user.getAsTag() + " (" + user.getId() + ")");
            event.reply("Der Report wurde erfolgreich ausgeführt").setEphemeral(true).queue();
            event.getGuild()
                    .getTextChannelById(config.getString("logChannel"))
                    .sendMessageEmbeds(embedBuilder.build())
                    .setActionRow(Button.secondary("close_bug", "Bug schließen")).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        super.onButtonInteraction(event);

        if (event.getButton().getId().equalsIgnoreCase("close_bug")) {
            event.getMessage().delete().queue();
            event.reply("Der Bug wurde erfolgreich geschlossen!").setEphemeral(true).queue();
        }
    }
}
