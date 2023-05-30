package com.slimebot.report.commands;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import com.slimebot.utils.Checks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public class ReportList extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (!(event.getName().equals("report_list"))) {return;}
        if (Checks.hasTeamRole(event.getMember(), event.getGuild())){
            EmbedBuilder noTeam = new EmbedBuilder()
                    .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                    .setColor(Main.embedColor(event.getGuild().getId()))
                    .setTitle(":exclamation: Error")
                    .setDescription("Der Befehl kann nur von einem Teammitglied ausgeführt werden!");
            event.replyEmbeds(noTeam.build()).setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setDescription("Nutze /report_detail oder das Dropdown menu um mehr infos zu einem Report zu bekommen.")
                .setColor(Main.embedColor(event.getGuild().getId()));

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

        if (ReportIdList.size() == 0){

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                    .setColor(Main.embedColor(event.getGuild().getId()))
                    .setTitle(":exclamation: Error: No Reports Found")
                    .setDescription("Es wurden keine Reports zu der Ausgewählten option gefunden!");
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            return;
        }

        MessageEmbed ed = embed.build();

        event.replyEmbeds(ed).addActionRow(DetailDropdownButton(ReportIdList)).queue();



    }

    private void addReportField(Report report, EmbedBuilder embed){
        embed.addField("Report #" + report.getId().toString(),
                report.getUser().getAsMention() + " wurde am `" + report.getTime().format(Main.dtf) + "` von " + report.getBy().getAsMention() + " gemeldet",
                false);
    }

    private StringSelectMenu DetailDropdownButton(ArrayList<Integer> reportList){
        StringSelectMenu.Builder btnBuilder = StringSelectMenu.create("detail_btn")
                .setPlaceholder("Details zu einem Report")
                .setMaxValues(1);

        for (Integer reportID:reportList) {
            btnBuilder.addOption("Report #" + reportID, reportID.toString(), "Details zum Report #" + reportID);
        }

        return btnBuilder.build();

    }






}
