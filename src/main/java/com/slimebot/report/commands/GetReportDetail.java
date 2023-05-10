package com.slimebot.report.commands;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import com.slimebot.report.assets.Type;
import com.slimebot.utils.Checks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class GetReportDetail extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (!(event.getName().equals("report_detail"))) {return;}
        if (Checks.hasTeamRole(event.getMember(), event.getGuild())){
            EmbedBuilder noTeam = new EmbedBuilder()
                    .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                    .setColor(Main.embedColor)
                    .setTitle(":exclamation: Error")
                    .setDescription("Der Befehl kann nur von einem Teammitglied ausgeführt werden!");
            event.replyEmbeds(noTeam.build()).queue();
            return;
        }

        OptionMapping id = event.getOption("id");
        MessageEmbed eb;

        for (Report report: Main.reports) {
            if (!(report.getId() == id.getAsInt())){continue;}

            eb = Report.getReportAsEmbed(report);

            Button closeBtn = Report.closeBtn(report.getId().toString());

            if (report.getStatus() == Status.CLOSED){
                event.replyEmbeds(eb).queue();
            } else {
                event.replyEmbeds(eb).setActionRow(closeBtn).queue();
            }


        }


    }
}
