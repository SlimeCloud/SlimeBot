package com.slimebot.commands;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.awt.*;

public class Bug extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (event.getName().equalsIgnoreCase("bug")) {
            TextInput textInput = TextInput
                    .create("bug:" + event.getInteraction().getMember().getId(), Config.getLocalProperty("bug.properties", "bug.label"), TextInputStyle.PARAGRAPH)
                    .setMinLength(Integer.valueOf(Config.getLocalProperty("bug.properties", "bug.minLength")))
                    .build();
            textInput.isRequired();

            Modal modal = Modal
                    .create("bug:" + event.getInteraction().getMember().getId(), Config.getLocalProperty("bug.properties", "bug.title"))
                    .addActionRow(textInput)
                    .build();
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        super.onModalInteraction(event);
        ModalMapping modalMapping = event.getInteraction().getValues().get(0);
        String label = Config.getLocalProperty("bug.properties", "bug.embedTitle");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.green);
        embedBuilder.setTitle(label);

        if (modalMapping.getId().contains("bug")) {
            User user = Main.getJDAInstance().retrieveUserById(modalMapping.getId().split(":")[1]).complete();
            embedBuilder.setDescription("Fehlerbeschreibung: \n\n");
            embedBuilder.appendDescription(modalMapping.getAsString() + "\n");
            embedBuilder.setFooter("Report von: " + user.getAsTag() + " (" + user.getId() + ")");
            event.reply(Config.getLocalProperty("bug.properties", "bug.successReport")).setEphemeral(true).queue();
            event.getGuild()
                    .getTextChannelById(Config.getProperty(Config.botPath + event.getGuild().getId() + "/config.yml", "logChannel"))
                    .sendMessageEmbeds(embedBuilder.build())
                    .setActionRow(Button.secondary("close", "Bug schließen")).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        super.onButtonInteraction(event);

        if (event.getButton().getId().equalsIgnoreCase("close")) {
            event.getMessage().delete().queue();
            event.reply(Config.getLocalProperty("bug.properties", "bug.closed")).setEphemeral(true).queue();
        }
    }
}
