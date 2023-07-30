package com.slimebot.commands.level.add;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "level", description = "Füge einem Nutzer level hinzu")
public class AddLevelCommand {
    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event,
                               @Option(name = "member") Member member,
                               @Option(name = "level", minValue = 1) int level
    ) {
        Level newLevel = Level.getLevel(member)
                .addXp(level, 0)
                .save();

        event.reply(member.getAsMention() + " wurden erfolgreich " + level + " level hinzugefügt!\nEr hat jetzt " + newLevel.level() + " level!").setEphemeral(true).queue();
    }
}
