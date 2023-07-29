package com.slimebot.commands.level.add;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "level", description = "Füge einem nutzer level hinzu")
public class AddLevelCommand {

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(name = "member") Member member, @Option(name = "level", minValue = 1) int level) {
        long userId = member.getIdLong();
        long guildId = event.getGuild().getIdLong();
        Level.addLevel(guildId, userId, level, 0, 0);
        event.reply(member.getAsMention() + " (" + member.getEffectiveName() + ") wurden erfolgreich " + level + " level hinzugefügt!\nEr hat jetzt " + Level.getLevel(guildId, userId).level() + " level!").setEphemeral(true).queue();
    }

}
