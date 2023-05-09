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
            event.reply("kein Teammitglied!").queue();
            return;
        }

        OptionMapping id = event.getOption("id");

        for (Report report: Main.reports) {
            if (!(report.getId() == id.getAsInt())){continue;}

            String TypeStr = "";
            switch (report.getType()) {
                case MSG -> TypeStr = "Nachricht";
                case USER -> TypeStr = "User";
            }

            String StatusStr = "";
            switch (report.getStatus()) {
                case CLOSED -> StatusStr = "Geschlossen";
                case OPEN -> StatusStr = "Offen";
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Main.embedColor)
                    .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                    .setTitle(":exclamation:  Details zu Report #" + id.getAsString())
                    .addField("Report Typ:", TypeStr, true)
                    .addField("Gemeldeter User:", report.getUser().getAsMention(), true)
                    .addField("Gemeldet von:", report.getBy().getAsMention(), true)
                    .addField("Gemeldet am:", report.getTime().format(Main.dtf) + "Uhr", true)
                    .addField("Status:", StatusStr, true);
            
            if (report.getType() == Type.MSG){
                embed.addField("Gemeldete Nachricht:", report.getMsgContent(), true);
            } else if (report.getType() == Type.USER) {
                embed.addField("Meldegrund:", report.getMsgContent(), true);
            }

            if (report.getStatus() == Status.CLOSED) {
                embed.addField("Verfahren:", report.getCloseReason(), true);
            }

            MessageEmbed eb = embed.build();

            Button closeBtn = Button.danger("close", "Close").withEmoji(Emoji.fromFormatted("🔒"));

            event.replyEmbeds(eb).setActionRow(closeBtn).queue();

        }


    }
}
