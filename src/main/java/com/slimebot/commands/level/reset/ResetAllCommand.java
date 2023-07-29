package com.slimebot.commands.level.reset;

import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "all", description = "Resete alle Statistiken eines nutzers")
public class ResetAllCommand {

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(name = "member") Member member) {
        String sb = ResetLevelCommand.execute(event, member) + "\n\n" +
                ResetMessagesCommand.execute(event, member) + "\n\n" +
                ResetXPCommand.execute(event, member);

        event.reply(sb).setEphemeral(true).queue();
    }

}