package com.slimebot.commands.level;

import com.slimebot.level.Level;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import de.mineking.discord.commands.annotated.option.defaultValue.IntegerDefault;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationCommand(name = "leaderboard", description = "Zeigt das Server Leaderboard an", feature = "level")
public class LeaderboardCommand {


    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(name = "limit", required = false, minValue = 2, maxValue = 20) @IntegerDefault(10) Integer limit) {
        List<Level> top = Level.getTop(event.getGuild().getIdLong(), limit);
        String labels = top.stream()
                .map(s -> event.getGuild().getMemberById(s.userId()).getEffectiveName())
                .collect(Collectors.joining(","));


        String data = top.stream()
                .map(Level::level)
                .map(String::valueOf)
                .collect(Collectors.joining(","));


        String url = String.format("https://quickchart.io/chart/render/%s?data1=%s&labels=%s", Main.config.level.leaderboardTemplate, data, labels).replace(" ", "%20");
        event.replyEmbeds(new EmbedBuilder()
                .setImage(url)
                .setColor(GuildConfig.getColor(event.getGuild()))
                .build()).queue();
    }
}
