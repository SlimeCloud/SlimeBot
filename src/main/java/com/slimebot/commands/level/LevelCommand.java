package com.slimebot.commands.level;

import com.slimebot.commands.level.add.AddLevelCommand;
import com.slimebot.commands.level.add.AddXPCommand;
import com.slimebot.commands.level.remove.RemoveLevelCommand;
import com.slimebot.commands.level.remove.RemoveXPCommand;
import com.slimebot.commands.level.reset.*;
import com.slimebot.level.Level;
import com.slimebot.main.CommandPermission;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "level", description = "Bearbeite die Level daten eines nutzers", guildOnly = true)
public class LevelCommand {

    public final CommandPermission permission = CommandPermission.TEAM;


    @ApplicationCommand(name = "add", description = "Füge einem nutzer XP oder Level hinzu", subcommands = {AddLevelCommand.class, AddXPCommand.class})
    public static class AddCommand {}

    @ApplicationCommand(name = "remove", description = "Entferne von einem nutzer XP oder Level", subcommands = {RemoveLevelCommand.class, RemoveXPCommand.class})
    public static class RemoveCommand {}

    @ApplicationCommand(name = "reset", description = "Resete Level, XP oder Nachrichten anzahl eines nutzers", subcommands = {ResetAllCommand.class, ResetLevelCommand.class, ResetMessagesCommand.class, ResetXPCommand.class})
    public static class ResetCommand {}

    @ApplicationCommand(name = "stats", description = "zeigt die nutzer Statistiken an")
    public static class StatsCommand {

        @ApplicationCommandMethod
        public void processCommand(SlashCommandInteractionEvent event, @Option(name = "member") Member member) {
            Level lvl = Level.getLevel(event.getGuild().getIdLong(), member.getIdLong());
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(member.getEffectiveName() + "`s Stats")
                    .addField("Level", String.valueOf(lvl.level()), false)
                    .addField("XP", String.valueOf(lvl.xp()), false)
                    .addField("Nachrichten", String.valueOf(lvl.messages()), false)
                    .setColor(GuildConfig.getColor(event.getGuild()))
                    .build()).setEphemeral(true).queue();
        }
    }
}
