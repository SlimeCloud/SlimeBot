package com.slimebot.report.contextmenus;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Type;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MsgReport extends ListenerAdapter {

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        super.onMessageContextInteraction(event);
        if (!(event.getName().equals("Report Message"))) {return;}

        int reportID = Main.reports.size() + 1;

        String msg = event.getTarget().getContentRaw();

        if ( msg.length() > 800) {
            msg = msg.substring(0,800) + "...";
        }

        String GuildId = event.getGuild().getId();
        String ChannelId = event.getChannel().getId();
        String MessageId = event.getTarget().getId();

        String msgWithLink = "[" + msg + "](https://discord.com/channels/"+GuildId+"/"+ChannelId+"/"+MessageId+")";

        Main.reports.add(Report.newReport(reportID, Type.MSG, event.getTarget().getMember(), event.getMember(), msgWithLink));


        event.reply(event.getTarget().getAuthor().getAsMention() + " wurde Reportet").setEphemeral(true).queue();


    }
}


