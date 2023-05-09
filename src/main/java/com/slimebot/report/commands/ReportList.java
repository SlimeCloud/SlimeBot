package com.slimebot.report.commands;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import com.slimebot.utils.Checks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public class ReportList extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (!(event.getName().equals("report_list"))) {return;}
        if (Checks.hasTeamRole(event.getMember(), event.getGuild())){
            event.reply("kein Teammitglied!").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setDescription("Nutze /report_detail oder das Dropdown menu um mehr infos zu einem Report zu bekommen.")
                .setColor(Main.embedColor);

        ArrayList<Integer> ReportIdList = new ArrayList<>();
        int fieldSize = 0;
        switch (event.getOption("status").getAsString()){
            case "all" -> {
                embed.setTitle("Eine Liste aller Reports");
                for (Report report:Main.reports) {
                    ReportIdList.add(report.getId());
                    if (fieldSize > 24){break;}
                    addReportField(report, embed);
                    fieldSize =+1;
                }
            }
            case "closed" -> {
                embed.setTitle("Eine Liste aller geschlossenen Reports");
                for (Report report:Main.reports) {
                    if (!(report.status == Status.CLOSED)){continue; }
                    ReportIdList.add(report.getId());
                    if (fieldSize > 24){break;}
                    addReportField(report, embed);
                    fieldSize =+1;
                }
            }
            case "open" -> {
                embed.setTitle("Eine Liste aller offenen Reports");
                for (Report report:Main.reports) {
                    if (!(report.status == Status.OPEN)){continue; }
                    ReportIdList.add(report.getId());
                    if (fieldSize > 24){break;}
                    addReportField(report, embed);
                    fieldSize =+1;
                }
            }

        }

        MessageEmbed ed = embed.build();

        event.replyEmbeds(ed).queue();



    }

    private void addReportField(Report report, EmbedBuilder embed){
        embed.addField("Report #" + report.getId().toString(),
                report.getUser().getAsMention() + " wurde am `" + report.getTime().format(Main.dtf) + "` von " + report.getBy().getAsMention() + " gemeldet",
                false);
    }




}
