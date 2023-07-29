package com.slimebot.commands.level;

import com.slimebot.level.Level;
import com.slimebot.level.RankCard;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.IOException;

@ApplicationCommand(name = "rank", description = "Zeigt dein Aktuelles Level und XP an", feature = "level")
public class RankCommand {

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(name = "user", required = false) User user) {
        if (user == null) user = event.getUser();

        if (user.isBot()) {
            event.reply("Bots wie " + user.getAsMention() + " können nicht leveln!").queue();
            return;
        }

        Level level = Level.getLevel(event.getGuild().getIdLong(), user.getIdLong());
        event.deferReply().queue();
        try {
            event.getHook().sendFiles(new RankCard(level).getFile()).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
