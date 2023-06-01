package com.slimebot.report.modals;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class CloseReport extends ListenerAdapter {
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
            EmbedBuilder noReport = new EmbedBuilder()
                    .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                    .setColor(Main.embedColor(event.getGuild().getId()))
                    .setTitle(":exclamation: Error: Report not Found")
                    .setDescription("Der Report #" + reportID + " konnte nicht gefunden werden!");
            event.replyEmbeds(noReport.build()).setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Main.embedColor(event.getGuild().getId()))
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setTitle("Report **#" +reportID + "** closed")
                .setDescription("Der Report mit der ID **#" + reportID + "** wurde erfolgreich geschlossen");
        MessageEmbed eb = embed.build();

        event.replyEmbeds(eb).queue();



    }
}
