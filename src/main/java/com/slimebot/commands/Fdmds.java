package com.slimebot.commands;

import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import com.slimebot.utils.SlimeEmoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Fdmds extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (event.getName().equalsIgnoreCase("fdmds")) {
            Modal modal = getFdmdsModal("fdmds", event.getMember().getId(), null);
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        super.onModalInteraction(event);
        if (!(event.getModalId().equals("fdmds" + event.getInteraction().getMember().getId())) && !(event.getModalId().equals("fdmds.edit" + event.getInteraction().getMember().getId())))return;

        if(event.getModalId().equals("fdmds" + event.getInteraction().getMember().getId())) {

            // get Log-channel
            TextChannel channel = getChannelFromConfig(event.getGuild().getId(), "fdmdsLogChannel");
            if(channel == null) {
                event.reply("Error: Channel wurde nicht gesetzt!").setEphemeral(true).queue();
                return;
            }



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
            embedBuilder.addField("Frage:", "Heute würde ich gerne von euch wissen, "+question, false);
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
            event.reply("Frage wurde bearbeitet.").setEphemeral(true).queue();
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
            Modal modal = getFdmdsModal("fdmds.edit", event.getMember().getId(), new String[] {question, choices});
            event.replyModal(modal).queue();
            return;
        }

        // Send Button
        if(event.getButton().getId().equals("fdmds.sendButton")) {
            // create text
            String text = "Einen Wunderschönen <:slimewave:1080225151104331817> ,\r\n";

            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            String question = embed.getFields().get(0).getValue();
            String choices = embed.getFields().get(1).getValue();
            String roleMention = "\n\n"+getRoleMentionFromConfig(event.getGuild().getId(), "fdmdsRoleId");


            text = text + " \r\n" + question + "\r\n \r\n" + choices+roleMention;

            // get fdmds-channel
            TextChannel channel = getChannelFromConfig(event.getGuild().getId(), "fdmdsChannel");
            if(channel == null) {
                event.reply("Error: Channel wurde nicht gesetzt!").setEphemeral(true).queue();
                return;
            }

            // Send and add reactions
            channel.sendMessage(text).queue(m -> {
                for(int i = 0; i<choices.lines().count(); i++) {
                    m.addReaction(SlimeEmoji.fromId(i+1).getEmoji()).queue();
                }

                event.reply("Frage verschickt!").setEphemeral(true).queue();
            });

            // Edit embed
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(embed.getTitle());
            embedBuilder.setColor(embed.getColor());
            embedBuilder.setFooter(embed.getFooter().getText());
            embedBuilder.addField("Frage:", embed.getFields().get(0).getValue(), false);
            embedBuilder.addField("Auswahlmöglichkeiten:", embed.getFields().get(1).getValue(), false);
            embedBuilder.addField("Versendet:", "Am "+ LocalDateTime.now().atZone(ZoneId.systemDefault()).format(Main.dtf), false);

            List<LayoutComponent> actionRow = new ArrayList<>();
            event.getMessage().editMessageComponents(actionRow).setEmbeds(embedBuilder.build()).queue();


        }
    }

    // idPrefix must be 'fdmds' or 'fdmds.edit'
    // value is only set if it is the edit Modal
    private Modal getFdmdsModal(String idPrefix, String memberId, String[] values) {
        if(idPrefix == null || memberId == null)return null;

        TextInput.Builder questionTextInput = TextInput
                .create(idPrefix + ".question" + memberId, "Deine Frage", TextInputStyle.SHORT)
                .setMinLength(10)
                .setMaxLength(150);
        if(values == null)questionTextInput.setPlaceholder("Welche Eissorte mögt ihr am liebsten?");
        if(values != null)questionTextInput.setValue(values[0]);
        questionTextInput.isRequired();

        TextInput.Builder choicesTextInput = TextInput
                .create(idPrefix + ".choices" + memberId, "Deine Antwortmöglichkeiten", TextInputStyle.PARAGRAPH)
                .setMinLength(10)
                .setMaxLength(800);
        if(values == null)choicesTextInput.setPlaceholder("Antworten mit ; trennen z.B. Erdbeere; Cookie; Schokolade");
        if(values != null)choicesTextInput.setValue(values[1]);
        choicesTextInput.isRequired();

        Modal modal = Modal
                .create(idPrefix + memberId, "Schlage eine fdmds Frage vor")
                .addActionRow(questionTextInput.build())
                .addActionRow(choicesTextInput.build())
                .build();

        return modal;
    }

    private TextChannel getChannelFromConfig(String guildId, String path) {
        if(guildId == null || path == null)return null;
        YamlFile config = Config.getConfig(guildId, "mainConfig");
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TextChannel channel;
        try {
            channel = Main.getJDAInstance().getGuildById(guildId).getTextChannelById(config.getString(path));
        } catch (IllegalArgumentException n){
            config.set(path, 0);
            try {
                config.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        return channel;
    }

    private String getRoleMentionFromConfig(String guildId, String path){
        if(guildId == null || path == null)return null;
        YamlFile config = Config.getConfig(guildId, "mainConfig");
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String roleMention="";
        if(!config.contains(path)){
            config.set(path, 0);
            try {
                config.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        try {
            roleMention = "<@&"+config.getLong(path)+">";
        } catch (IllegalArgumentException n){
            config.set(path, 0);
            try {
                config.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        return roleMention;
    }
}
