package com.slimebot.commands.level.remove;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "level", description = "Entferne level von einem nutzer")
public class RemoveLevelCommand {

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(name = "member") Member member, @Option(name = "level", minValue = 1) int level) {
        long userId = member.getIdLong();
        long guildId = event.getGuild().getIdLong();
        Level lvl = Level.getLevel(guildId, userId);
        if (level>lvl.level()) {
            event.reply(member.getAsMention() + " (" + member.getEffectiveName() + ") hat nur " + lvl.level() + " Level. du kannst ihm also maximal " + lvl.level() + " Level entfernen!").setEphemeral(true).queue();
            return;
        }
        Level.addLevel(guildId, userId, level*-1, 0, 0);
        event.reply(member.getAsMention() + " (" + member.getEffectiveName() + ") wurden erfolgreich " + level + " level entfernt!\nEr ist jetzt level " + (lvl.level()-level) + "!").setEphemeral(true).queue();
    }

}
