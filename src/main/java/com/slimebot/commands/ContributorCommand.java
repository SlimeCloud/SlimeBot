package com.slimebot.commands;

import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import de.mineking.discord.events.Listener;
import de.mineking.discord.events.interaction.ButtonHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationCommand(name = "contributor", description = "Bewerbe dich für die Contributor-Rolle wenn du am unserem Open Source Projekt mitgearbeitet hast", guildOnly = true)
public class ContributorCommand {
	public final static Logger logger = LoggerFactory.getLogger(ContributorCommand.class);

	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(name = "user", description = "Wie heißt du auf GitHub?") String user,
	                           @Option(name = "link", description = "Der GitHub link zu deinem PR") String link
	) {
		Main.database.getChannel(event.getGuild(), DatabaseField.LOG_CHANNEL).ifPresentOrElse(
				channel -> {
					channel.sendMessage(link).addEmbeds(
							new EmbedBuilder()
									.setTitle("Neue Contributor Bewerbung")
									.setAuthor(event.getUser().getName(), null, event.getUser().getEffectiveAvatarUrl())
									.addField("GitHubname:", user, false)
									.addField("Discord User:", event.getUser().getAsMention(), false)
									.addField("PR:", link, false)
									.setColor(Main.database.getColor(event.getGuild()))
									.build()
					).addActionRow(
							Button.success("contributor:accept", "Annehmen"),
							Button.danger("contributor:reject", "Ablehnen")
					).queue();
					event.reply("Vielen dank für deine Mithilfe!\nUnser Team wird dies Prüfen und dir die dann die Rolle zuweisen.").setEphemeral(true).queue();
				},
				() -> {
					event.reply("**Error:** Der Befehl wurde nicht korrekt Konfiguriert.").queue();
					logger.error("Kein LOG_CHANNEL gesetzt");
				}
		);
	}

	@Listener(type = ButtonHandler.class, filter = "contributor:accept")
	public void handleAccept(ButtonInteractionEvent event) {
		UserSnowflake user = getUser(event.getMessage().getEmbeds().get(0));

		Main.database.getRole(event.getGuild(), DatabaseField.CONTRIBUTOR_ROLE).ifPresentOrElse(
				role -> {
					event.getGuild().addRoleToMember(user, role).reason("Hat an GitHub Projekt mitgearbeitet").queue();

					event.getMessage().delete().queue();
					Main.jdaInstance.openPrivateChannelById(user.getIdLong())
							.flatMap(channel -> channel.sendMessage("Dir wurde die ContributorRolle auf dem SlimeCloud Discord gegeben."))
							.queue();
					event.reply(user.getAsMention() + " wurde die Contributor Rolle gegeben.").queue();
				},
				() -> {
					event.reply("**Error:** Der Befehl wurde nicht korrekt Konfiguriert.").queue();
					logger.error("Keine CONTRIBUTOR_ROLE gesetzt");
				}
		);
	}

	@Listener(type = ButtonHandler.class, filter = "contributor:reject")
	public void handleDeny(ButtonInteractionEvent event) {
		UserSnowflake user = getUser(event.getMessage().getEmbeds().get(0));

		event.getMessage().delete().queue();
		Main.jdaInstance.openPrivateChannelById(user.getIdLong())
				.flatMap(channel -> channel.sendMessage("Dir wurde die ContributorRolle auf dem SlimeCloud Discord leider **nicht** gegeben."))
				.queue();
		event.reply(user.getAsMention() + " wurde die Contributor Rolle nicht gegeben.").queue();
	}

	public static UserSnowflake getUser(MessageEmbed embed) {
		return UserSnowflake.fromId(embed.getAuthor().getIconUrl().split("/")[4]); //Avatar Pattern: "https://cdn.discordapp.com/avatars/%s/%s.%s"
	}
}