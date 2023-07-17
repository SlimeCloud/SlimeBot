package com.slimebot.commands;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.commands.annotated.option.Option;
import de.mineking.discord.events.interaction.ButtonHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@ApplicationCommand(name = "contributor", description = "Bewerbe dich für die Contributor-Rolle wenn du am unserem Open Source Projekt mitgearbeitet hast")
public class ContributorCommand {
    public final static Logger logger = LoggerFactory.getLogger(ContributorCommand.class);
    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event,
                               @Option(name = "user", description = "Wie heißt du auf GitHub?") String user,
                               @Option(name = "link", description = "Der GitHub link zu deinem PR") String link) {

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Neue Contributor Bewerbung")
                .addField("GitHubname:", user, false)
                .addField("Discord User:", event.getUser().getAsMention(), false)
                .addField("PR:", link, false)
                .setColor(Main.embedColor(event.getGuild().getId()));

        YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TextChannel logChannel = Main.jdaInstance.getTextChannelById(config.getString("logChannel"));
        if (logChannel == null){
            event.reply("**Error:** Der Befehl wurde nicht korrekt Konfiguriert.").queue();
            logger.error("Channel not Found");
            return;
        }

        logChannel.sendMessage(link).addEmbeds(embed.build()).addActionRow(Button.success("contributor:accept", "Annehmen"), Button.success("contributor:reject", "Ablehnen")).queue();
        event.reply("Vielen dank für deine Mithilfe!\nUnser Team wird dies Prüfen und dir die dann die Rolle zuweisen.").setEphemeral(true).queue();
    }

    @WhenFinished()
    public void setup(DiscordUtils manager) {
        manager.getEventManager().registerHandler(new ButtonHandler("contributor:accept", event -> {
            Member member = event.getGuild().getMemberById(event.getMessage().getEmbeds().get(0).getFields().get(1).getValue().substring(2,20));
            Role role = event.getGuild().getRoleById(contributorRoleID(event.getGuild().getId()));

            event.getGuild().addRoleToMember(member, role).reason("Hat an GitHub Projekt mitgearbeitet").queue();
            member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Dir wurde die ContributorRolle auf dem SlimeCloud Discord gegeben.")).queue();
            event.getMessage().delete().queue();
            event.reply(member.getAsMention() + " wurde die Contributor Rolle gegeben.").queue();
        }));

        manager.getEventManager().registerHandler(new ButtonHandler("contributor:reject", event -> {
            Member member = event.getGuild().getMemberById(event.getMessage().getEmbeds().get(0).getFields().get(1).getValue().substring(2,20));

            member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Dir wurde die ContributorRolle auf dem SlimeCloud Discord leider **nicht** gegeben.")).queue();
            event.getMessage().delete().queue();
            event.reply(member.getAsMention() + " wurde die Contributor Rolle nicht gegeben.").queue();
        }));
    }

    private static String contributorRoleID(String guildID) {
        YamlFile config = Config.getConfig(guildID, "mainConfig");
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String roleId = config.getString("contributorRole");
        if (roleId == null || roleId.equals("0")){
            logger.error("Role not Found");
            return null;
        }
        return roleId;
    }
}