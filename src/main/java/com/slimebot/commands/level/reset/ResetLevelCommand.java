package com.slimebot.commands.level.reset;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "level", description = "Setzt die Level eines Nutzers zurück")
public class ResetLevelCommand {
    public static String execute(Member member) {
        Level.getLevel(member).setXp(0, 0);
        return "Level von " + member.getAsMention() + " zurückgesetzt";
    }

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(name = "member") Member member) {
        event.reply(execute(member)).setEphemeral(true).queue();
    }
}
