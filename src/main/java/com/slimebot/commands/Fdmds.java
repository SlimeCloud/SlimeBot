package com.slimebot.commands;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import com.sun.jdi.Field;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
            TextInput textInput = TextInput
                    .create("fdmds:" + event.getInteraction().getMember().getId(), "Dein Vorschlag", TextInputStyle.PARAGRAPH)
                    .setMinLength(10)
                    .build();
            textInput.isRequired();

            Modal modal = Modal
                    .create("fdmds" + event.getInteraction().getMember().getId(), "Schlage eine fdmds Frage vor")
                    .addActionRow(textInput)
                    .build();
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        super.onModalInteraction(event);
        if (!(event.getModalId().equals("fdmds" + event.getInteraction().getMember().getId())))return;

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

        // Get Content
        String content = event.getInteraction().getValue("fdmds:" + event.getInteraction().getMember().getId()).getAsString();

        // Create and send Embed
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Main.embedColor(event.getGuild().getId()));
        embedBuilder.setTitle("Frag doch mal den Schleim");
        embedBuilder.setFooter("Vorschlag von: " + user.getAsTag() + " (" + user.getId() + ")");
        embedBuilder.addField("Vorschlag:", content, false);
        channel.sendMessageEmbeds(embedBuilder.build()).queue();

        // Send User Feedback
        event.reply("Vorschlag erfolgreich verschickt!").setEphemeral(true).queue();
    }
}
