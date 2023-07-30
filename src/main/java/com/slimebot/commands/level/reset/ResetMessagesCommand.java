package com.slimebot.commands.level.reset;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "messages", description = "Setzt die Nachrichten-Statistiken eines Nutzers zurück")
public class ResetMessagesCommand {
    public static String execute(Member member) {
        Level.getLevel(member).setMessages(0).save();
        return "Nachrichten Statistiken von" + member.getAsMention() + " zurückgesetzt";
    }

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(description = "Der Nutzer, dessen Nachrichten Statistiken zurückgesetzt werden sollen") Member user) {
        event.reply(execute(user)).setEphemeral(true).queue();
    }
}
