package com.slimebot.report.commands;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Type;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

public class ReportCmd extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (!(event.getName().equals("report"))) {return;}

        int reportID = Main.reports.size() + 1;

        OptionMapping user = event.getOption("user");
        OptionMapping description = event.getOption("beschreibung");

        Main.reports.add(Report.newReport(reportID, Type.USER, user.getAsMember(), event.getMember(), description.getAsString()));

        event.reply(user.getAsMentionable().getAsMention() + " wurde Reportet").setEphemeral(true).queue();
        //ToDo add log msg with Close btn




    }
}
