package com.slimebot.commands;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import com.slimebot.utils.SlimeEmoji;
import com.sun.jdi.Field;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;

public class Fdmds extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (event.getName().equalsIgnoreCase("fdmds")) {
            TextInput questionTextInput = TextInput
                    .create("fdmds.question" + event.getInteraction().getMember().getId(), "Deine Frage", TextInputStyle.SHORT)
                    .setMinLength(10)
                    .setPlaceholder("Was ist euer lieblings Eis?")
                    .build();
            questionTextInput.isRequired();

            TextInput choicesTextInput = TextInput
                    .create("fdmds.choices" + event.getInteraction().getMember().getId(), "Deine Antwortmöglichkeiten", TextInputStyle.PARAGRAPH)
                    .setMinLength(10)
                    .setPlaceholder("Schoko ; Erdbeere ; Vanille")
                    .build();
            choicesTextInput.isRequired();

            Modal modal = Modal
                    .create("fdmds" + event.getInteraction().getMember().getId(), "Schlage eine fdmds Frage vor")
                    .addActionRow(questionTextInput)
                    .addActionRow(choicesTextInput)
                    .build();
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        super.onModalInteraction(event);
        if (!(event.getModalId().equals("fdmds" + event.getInteraction().getMember().getId())) && !(event.getModalId().equals("fdmds.edit" + event.getInteraction().getMember().getId())))return;

        if(event.getModalId().equals("fdmds" + event.getInteraction().getMember().getId())) {
            // get Log-channel
            YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");
            try {
                config.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            TextChannel channel = event.getGuild().getTextChannelById(config.getString("logChannel"));

            // Get User
            User user = Main.getJDAInstance().retrieveUserById(event.getMember().getId()).complete();

            // Get Contents
            String question = event.getInteraction().getValue("fdmds.question" + event.getInteraction().getMember().getId()).getAsString();
            String[] choices = event.getInteraction().getValue("fdmds.choices" + event.getInteraction().getMember().getId()).getAsString().split(";");

            if(choices.length <= 1) {
                event.reply("Du musst mindestens 2 Antwortmöglichkeiten angeben!").setEphemeral(true).queue();
                return;
            }
            if(choices.length > 9) {
                event.reply("Du kannst maximal 9 Antwortmöglichkeiten angeben!").setEphemeral(true).queue();
                return;
            }
            String choicesStr = "";
            for(int i = 0; i<choices.length; i++) {
                choicesStr += SlimeEmoji.fromId(i+1).getAsString()
                        + " -> "
                        + choices[i].strip()
                        + "\r\n";
            }

            // Create Buttons
            Button editButton = Button.secondary("fdmds.editButton", "Edit");
            Button sendButton = Button.danger("fdmds.sendButton", "Senden");

            // Create and send Embed
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Main.embedColor(event.getGuild().getId()));
            embedBuilder.setTitle("Frag doch mal den Schleim");
            embedBuilder.setFooter("Vorschlag von: " + user.getAsTag() + " (" + user.getId() + ")");
            embedBuilder.addField("Frage:", question, false);
            embedBuilder.addField("Auswahlmöglichkeiten:", choicesStr, false);
            channel.sendMessageEmbeds(embedBuilder.build())
                    .addActionRow(editButton, sendButton).queue();

            // Send User Feedback
            event.reply("Vorschlag erfolgreich verschickt!").setEphemeral(true).queue();
        }

        if(event.getModalId().equals("fdmds.edit" + event.getInteraction().getMember().getId())) {
            // Get Contents
            String question = event.getInteraction().getValue("fdmds.edit.question" + event.getInteraction().getMember().getId()).getAsString();
            String choices = event.getInteraction().getValue("fdmds.edit.choices" + event.getInteraction().getMember().getId()).getAsString();

            // Edit embed
            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(embed.getTitle());
            embedBuilder.setColor(embed.getColor());
            embedBuilder.setFooter(embed.getFooter().getText());
            embedBuilder.addField("Frage:", question, false);
            embedBuilder.addField("Auswahlmöglichkeiten:", choices, false);

            event.getMessage().editMessage("Edited").setEmbeds(embedBuilder.build()).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
        if(!event.getButton().getId().equals("fdmds.editButton") && !event.getButton().getId().equals("fdmds.sendButton"))return;

        // Edit Button
        if(event.getButton().getId().equals("fdmds.editButton")) {
            // Get Contents
            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            String question = embed.getFields().get(0).getValue();
            String choices = embed.getFields().get(1).getValue();

            // Crate Edit Modal
            TextInput questionTextInput = TextInput
                    .create("fdmds.edit.question" + event.getInteraction().getMember().getId(), "Frage", TextInputStyle.PARAGRAPH)
                    .setMinLength(10)
                    .setValue(question)
                    .build();
            questionTextInput.isRequired();

            TextInput choicesTextInput = TextInput
                    .create("fdmds.edit.choices" + event.getInteraction().getMember().getId(), "Antwortmöglichkeiten", TextInputStyle.PARAGRAPH)
                    .setMinLength(10)
                    .setValue(choices)
                    .build();
            choicesTextInput.isRequired();

            Modal modal = Modal
                    .create("fdmds.edit" + event.getInteraction().getMember().getId(), "Editiere diesen vorschlag")
                    .addActionRow(questionTextInput)
                    .addActionRow(choicesTextInput)
                    .build();
            event.replyModal(modal).queue();
            return;
        }

        // Send Button
        //ToDo abfrage ob channel gesendet wurde
        if(event.getButton().getId().equals("fdmds.sendButton")) {
            // create text
            String text = "Einen Wunderschönen <:slimewave:1080225151104331817> ,\r\n";

            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            String question = embed.getFields().get(0).getValue();
            String choices = embed.getFields().get(1).getValue();

            text = text + " \r\n" + question + "\r\n \r\n" + choices;

            // get fdmds-channel
            YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");
            try {
                config.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            TextChannel channel = event.getGuild().getTextChannelById(config.getString("fdmdsChannel"));

            // Send and add reactions
            channel.sendMessage(text).queue(m -> {
                for(int i = 0; i<choices.lines().count(); i++) {
                    m.addReaction(SlimeEmoji.fromId(i+1).getEmoji()).queue();
                }

                event.reply("Frage verschickt!").setEphemeral(true).queue();
            });
        }
    }
}
