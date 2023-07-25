package com.slimebot.commands.level.reset;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "level", description = "Resete die Level eines nutzers")
public class ResetLevelCommand {


    public static String execute(SlashCommandInteractionEvent event, @Option(name = "member") Member member) {
        try {
            long userId = member.getIdLong();
            long guildId = event.getGuild().getIdLong();
            Level lvl = Level.getLevel(guildId, userId);
            Level.addLevel(guildId, userId, lvl.level()*-1, 0, 0);
            return "Level von " + member.getAsMention() + " (" + member.getEffectiveName() + ") erfolgreich zurückgesetzt!";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Level von " + member.getAsMention() + " (" + member.getEffectiveName() + ") konnte nicht zurückgesetzt werden!";
    }


    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, Member member) {
        event.reply(execute(event, member)).setEphemeral(true).queue();
    }

}
