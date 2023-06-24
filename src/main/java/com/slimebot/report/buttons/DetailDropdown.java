package com.slimebot.report.buttons;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import com.slimebot.report.assets.Type;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.simpleyaml.configuration.file.YamlFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public class DetailDropdown extends ListenerAdapter {
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        super.onStringSelectInteraction(event);
        if (!(event.getComponentId().equals("detail_btn"))) {return;}

        String id = event.getValues().get(0);
        System.out.println("id = " + id);
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
