package com.slimebot.commands.level.remove;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "xp", description = "Entferne xp von einem nutzer")
public class RemoveXPCommand {

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(name = "member") Member member, @Option(name = "xp", minValue = 1) int xp) {
        long userId = member.getIdLong();
        long guildId = event.getGuild().getIdLong();
        Level lvl = Level.getLevel(guildId, userId);
        if (xp>lvl.xp()) {
            event.reply(member.getAsMention() + " (" + member.getEffectiveName() + ") hat nur " + lvl.xp() + " XP. du kannst ihm also maximal " + lvl.xp() + " XP entfernen!").setEphemeral(true).queue();
            return;
        }
        Level.addLevel(guildId, userId, 0, xp*-1, 0);
        event.reply(member.getAsMention() + " (" + member.getEffectiveName() + ") wurden erfolgreich " + xp + " XP entfernt!\nEr hat jetzt " + (lvl.xp()-xp) + " xp!").setEphemeral(true).queue();
    }

}
