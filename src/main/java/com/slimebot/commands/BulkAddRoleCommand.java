package com.slimebot.commands;


import com.slimebot.main.CommandPermission;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;
import java.util.List;

@ApplicationCommand(name = "role_check", description = "Geht ALLE Mitglieder durch und gibt ihnen eine Rolle", guildOnly = true)
public class BulkAddRoleCommand {
    public final CommandPermission permission = CommandPermission.ROLE_MANAGE;

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event,
                               @Option(description = "Auf welche Rolle sollen die User überprüft werden?") Role role,
                               @Option(description = "Sollen Bots mit überprüft werden?") boolean bots
    ) {
        List<Member> members = event.getGuild().getMembers().stream()
                .filter(m -> !m.getUser().isBot() || bots)
                .filter(m -> !m.getRoles().contains(role))
                .toList();

        members.forEach(m -> event.getGuild().addRoleToMember(m, role).queue());

        event.replyEmbeds(
                new EmbedBuilder()
                        .setTimestamp(Instant.now())
                        .setColor(GuildConfig.getColor(event.getGuild()))
                        .setTitle(":white_check_mark: Rollen Verteilt")
                        .setDescription("Die Rolle " + role.getAsMention() + " wurde " + members.size() + " Membern gegeben!")
                        .build()
        ).setEphemeral(true).queue();
    }
}
