package com.slimebot.commands.level;

import com.slimebot.level.Level;
import com.slimebot.main.Main;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

@ApplicationCommand(name = "leaderboard", guildOnly = true)
public class LeaderboardCommand {


    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(name = "limit", required = false) Integer limit) {
        if(limit == null) limit = 10;
        List<Level> top = Level.getTop(event.getGuild().getIdLong(), limit);
        StringBuilder labels = new StringBuilder();
        StringBuilder data = new StringBuilder();
        top.forEach(s -> {
            labels.append(event.getJDA().getUserById(s.userId()).getName()).append(",");
            data.append(s.level()).append(",");
        });


        String url = String.format("https://quickchart.io/chart/render/%s?data1=%s&labels=%s", "zm-76d73da4-d559-401b-92c1-d256b8c98699", data.substring(0, data.length() - 1), labels.substring(0, labels.length() - 1)).replace(" ", "%20");
        System.out.println(url);
        MessageEmbed embed = new EmbedBuilder()
                .setImage(url)
                .setColor(Main.embedColor(event.getGuild().getId()))
                .build();
        event.replyEmbeds(embed).queue();
    }
}
