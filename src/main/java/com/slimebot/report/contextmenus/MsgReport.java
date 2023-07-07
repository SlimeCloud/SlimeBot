package com.slimebot.report.contextmenus;

import com.slimebot.utils.Config;
import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Type;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MsgReport extends ListenerAdapter {

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        super.onMessageContextInteraction(event);
        if (!(event.getName().equals("Report Message"))) {return;}
        if (Main.blocklist(event.getGuild().getId()).contains(event.getMember().getId())) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                    .setColor(Main.embedColor(event.getGuild().getId()))
                    .setTitle(":exclamation: Error: Blocked")
                    .setDescription("Du wurdest gesperrt, so dass du keine Reports mehr erstellen kannst");
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            return;
        }

        YamlFile reportFile = Config.getConfig(event.getGuild().getId(), "reports");
        try {
            reportFile.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int reportID = reportFile.getConfigurationSection("reports").size() + 1;

        String msg = event.getTarget().getContentRaw();

        if ( msg.length() > 800) {
            msg = msg.substring(0,800) + "...";
        }

        String GuildId = event.getGuild().getId();
        String ChannelId = event.getChannel().getId();
        String MessageId = event.getTarget().getId();

        String msgWithLink = "[" + msg + "](https://discord.com/channels/"+GuildId+"/"+ChannelId+"/"+MessageId+")";

        Report.save(event.getGuild().getId(), Report.newReport(reportID, Type.MSG, event.getTarget().getMember(), event.getMember(), msgWithLink));

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setColor(Main.embedColor(event.getGuild().getId()))
                .setTitle(":white_check_mark: Report Erfolgreich")
                .setDescription(event.getTarget().getAuthor().getAsMention() + " wurde erfolgreich gemeldet");
        event.replyEmbeds(embedBuilder.build()).queue();
        Report.log(reportID, event.getGuild().getId());


    }
}


