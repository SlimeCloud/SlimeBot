package com.slimebot.report.buttons;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class Close extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        super.onButtonInteraction(event);

        if (!(event.getButton().getId().equals("close"))) {return;}

        String[] nameSplit = event.getButton().getLabel().split(" ");
        String reportID = nameSplit[1];


        TextInput reason = TextInput.create("reason", "Wie Wurde mit dem Report Verfahren?", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("z. B. Warn, Kick, Mute, Ban, Nichts etc..")
                .build();

        TextInput id = TextInput.create("id", "ID des Reports der geschlossen wird", TextInputStyle.SHORT)
                .setRequired(true)
                .setValue(reportID)
                .setPlaceholder("Dieses Feld wird automatisch ausgefüllt!")
                .build();



        Modal close = Modal.create("close", "Close Report")
                .addActionRow(reason)
                .addActionRow(id)
                .build();


        event.replyModal(close).queue();

    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        super.onModalInteraction(event);

        if (!(event.getModalId().equals("close"))) {return;}

        String reasonInput = event.getValue("reason").getAsString();
        int reportID = Integer.valueOf(event.getValue("id").getAsString());
        boolean reportFound = false;

        for (Report report: Main.reports) {
            if (!(Objects.equals(report.getId(), reportID))){continue;}
            reportFound = true;
            report.setCloseReason(reasonInput);
            report.setStatus(Status.CLOSED);
        }

        if (!reportFound){
            event.reply("**Error:** Report #" + reportID + " not found!").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Main.embedColor)
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setTitle("Report **#" +reportID + "** closed")
                .setDescription("Der Report mit der ID **#" + reportID + "** wurde erfolgreich geschlossen");
        MessageEmbed eb = embed.build();

        event.replyEmbeds(eb).queue();



    }
}
