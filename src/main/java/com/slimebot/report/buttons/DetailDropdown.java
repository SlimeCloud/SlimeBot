package com.slimebot.report.buttons;

import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class DetailDropdown extends ListenerAdapter {
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        super.onStringSelectInteraction(event);
        if (!(event.getComponentId().equals("detail_btn"))) {return;}

        String id = event.getValues().get(0);
        MessageEmbed eb;


        Report report = Report.get(event.getGuild().getId(), Integer.valueOf(id));

        eb = Report.getReportAsEmbed(report, event.getGuild().getId());
        if (report.getStatus() == Status.CLOSED){
            event.replyEmbeds(eb).queue();
        } else {
            Button closeBtn = Report.closeBtn(report.getId().toString());
            event.replyEmbeds(eb).setActionRow(closeBtn).queue();
        }




    }
}
