package com.slimebot.commands.level.add;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "xp", description = "Füge einem nutzer xp hinzu")
public class AddXPCommand {

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, @Option(name = "member") Member member, @Option(name = "xp", minValue = 1) int xp) {
        long userId = member.getIdLong();
        long guildId = event.getGuild().getIdLong();
        Level.addLevel(guildId, userId, 0, xp, 0);
        Level lvl = Level.getLevel(guildId, userId);
        event.reply(member.getAsMention() + " (" + member.getEffectiveName() + ") wurden erfolgreich " + xp + " xp hinzugefügt!\nEr hat jetzt " + lvl.xp() + " xp, und ist level " + lvl.level() + "!").setEphemeral(true).queue();
    }

}
