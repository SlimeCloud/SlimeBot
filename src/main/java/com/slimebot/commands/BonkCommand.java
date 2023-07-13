package com.slimebot.commands;


import com.slimebot.main.Main;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "bonk", description = "Bonke eine Person")
public class BonkCommand {
    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(name = "user", description = "Wen willst du bonken?") User user) {
        Emoji bonkEmoji = Main.jdaInstance.getEmojiById("1128781773908758638");

        event.reply(event.getUser().getAsMention() + " --> " + bonkEmoji.getFormatted() + " <-- " + user.getAsMention()).queue();

    }
}
