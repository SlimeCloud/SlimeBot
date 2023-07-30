package com.slimebot.commands.level.remove;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "xp", description = "Entferne xp von einem Nutzer")
public class RemoveXPCommand {
    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event,
                               @Option(name = "member") Member member,
                               @Option(name = "xp", minValue = 1) int xp
    ) {
        Level current = Level.getLevel(member);

        if (xp > current.xp()) {
            event.reply(member.getAsMention() + " hat nur " + current.xp() + " XP. du kannst ihm also maximal " + current.xp() + " XP entfernen!").setEphemeral(true).queue();
            return;
        }

        current = current.addXp(0, -1 * xp).save();

        event.reply(member.getAsMention() + " wurden erfolgreich " + xp + " XP entfernt!\nEr hat jetzt " + current.xp() + " xp!").setEphemeral(true).queue();
    }
}
