package com.slimebot.commands.level.remove;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "level", description = "Entferne level von einem Nutzer")
public class RemoveLevelCommand {

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event,
                               @Option(name = "member") Member member,
                               @Option(name = "level", minValue = 1) int level
    ) {
        Level current = Level.getLevel(member);

        if (level > current.level()) {
            event.reply(member.getAsMention() + "   hat nur " + current.level() + " Level. du kannst ihm also maximal " + current.level() + " Level entfernen!").setEphemeral(true).queue();
            return;
        }

        current = current.addXp(-1 * level, 0).save();

        event.reply(member.getAsMention() + " wurden erfolgreich " + level + " level entfernt!\nEr ist jetzt level " + current.level() + "!").setEphemeral(true).queue();
    }

}
