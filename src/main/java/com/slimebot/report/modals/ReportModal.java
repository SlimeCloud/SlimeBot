package com.slimebot.report.modals;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;

public class ReportModal extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        super.onModalInteraction(event);

        if (!(event.getModalId().equals("userReport"))){return;}

        Report currentReport = null;

        ModalMapping id = event.getValue("id");
        ModalMapping description = event.getValue("usrDescr");

        YamlFile reportFile = Config.getConfig(event.getGuild().getId(), "reports");
        try {
            reportFile.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ConfigurationSection reportSection = reportFile.getConfigurationSection("reports");
        ArrayList<Report> allReports = new ArrayList<>();
        for (int ids = 2; ids <= reportSection.size() ; ids++) {
            allReports.add(Report.get(event.getGuild().getId(), ids));
        }


        for (Report report:allReports) {
            if (Objects.equals(String.valueOf(report.getId()), id.getAsString()) && event.getMember() == report.getBy()) {
                currentReport = report;
                break;
            }
        }

        currentReport.setMsgContent(description.getAsString());

        Report.save(event.getGuild().getId(), currentReport);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setColor(Main.embedColor(event.getGuild().getId()))
                .setTitle(":white_check_mark: Report Erfolgreich")
                .setDescription(currentReport.getUser().getAsMention() + " wurde erfolgreich gemeldet");
        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        Report.log(currentReport.getId(), event.getGuild().getId());

    }
}
