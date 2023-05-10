package com.slimebot.report.contextmenus;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Type;
import com.slimebot.utils.Checks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class UserReport extends ListenerAdapter {

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        super.onUserContextInteraction(event);

        if (!(event.getName().equals("Report User"))) {return;}
        if (Checks.isReportBlocked(event.getMember(), (TextChannel) event.getChannel())) {return;}


        int reportID = Main.reports.size() + 1;


        Main.reports.add(Report.newReport(reportID, Type.USER, event.getTargetMember(), event.getMember(), "None"));


        TextInput userReportDescription = TextInput.create("usrDescr", "Warum möchtest du diese Person Reporten?", TextInputStyle.SHORT)
                .setMinLength(15)
                .setMaxLength(500)
                .setRequired(true)
                .setPlaceholder("Hier deine Begründung")
                .build();

        TextInput saveId = TextInput.create("id", "Bitte die Report ID nicht ändern!", TextInputStyle.SHORT)
                .setRequired(true)
                .setValue(String.valueOf(reportID))
                .setPlaceholder("Diese feld wird automatisch ausgefüllt!")
                .build();



        Modal userReport = Modal.create("userReport", "User Reporten")
                .addActionRow(userReportDescription)
                .addActionRow(saveId)
                .build();

        event.replyModal(userReport).queue();

    }

}
