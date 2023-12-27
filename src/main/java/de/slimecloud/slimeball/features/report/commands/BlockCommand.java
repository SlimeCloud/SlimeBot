package de.slimecloud.slimeball.features.report.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.list.ListManager;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@ApplicationCommand(name = "block", description = "Verwaltet vom Report-System ausgeschlossene Nutzer")
public class BlockCommand {
	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull Command<ICommandContext> command, @NotNull ListManager<ICommandContext> list) {
		//Register subcommand to display all report blocks
		command.addSubcommand(list.createCommand(state -> bot.getReportBlocks()).withDescription("Zeigt alle ausgeschlossenen Mitglieder"));
	}

	@ApplicationCommand(name = "add", description = "Fügt einen Nutzer zur Block-Liste hinzu, sodass er keine Meldungen mehr machen kann")
	public static class AddCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Der Nutzer, der blockiert werden soll") Member user,
		                           @Option(description = "Der Grund, warum der Nutzer blockiert wird") String reason
		) {
			//Check current status
			if (bot.getReportBlocks().isBlocked(user).isPresent()) {
				event.replyEmbeds(new EmbedBuilder()
						.setTitle(":exclamation: Bereits blockiert")
						.setColor(bot.getColor(event.getGuild()))
						.setDescription(user.getAsMention() + " ist bereits blockiert")
						.setTimestamp(Instant.now())
						.build()
				).setEphemeral(true).queue();
				return;
			}

			//Insert and check for successful insertion
			if (bot.getReportBlocks().blockUser(event.getUser(), user, reason)) {
				event.replyEmbeds(new EmbedBuilder()
						.setTitle(":white_check_mark: Erfolgreich blockiert")
						.setColor(bot.getColor(event.getGuild()))
						.setDescription(user.getAsMention() + " wurde blockiert und kann nun keine Reports mehr erstellen")
						.setTimestamp(Instant.now())
						.build()
				).setEphemeral(true).queue();
			}
		}
	}

	@ApplicationCommand(name = "remove", description = "Gibt einen Nutzer wieder für Meldungen frei")
	public static class RemoveCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Der Nutzer, der ent-blockiert werden soll") Member user
		) {
			//Check current status
			if (bot.getReportBlocks().isBlocked(user).isEmpty()) {
				event.replyEmbeds(new EmbedBuilder()
						.setTitle(":exclamation: Nicht blockiert")
						.setColor(bot.getColor(event.getGuild()))
						.setDescription(user.getAsMention() + " konnte nicht in der Blockliste gefunden werden!")
						.setTimestamp(Instant.now())
						.build()
				).setEphemeral(true).queue();
				return;
			}

			//Delete block and send confirmation
			bot.getReportBlocks().unblock(user);

			event.replyEmbeds(new EmbedBuilder()
					.setTitle(":white_check_mark: Blockierung aufgehoben")
					.setColor(bot.getColor(event.getGuild()))
					.setDescription(user.getAsMention() + " kann nun wieder Reports erstellen")
					.setTimestamp(Instant.now())
					.build()
			).setEphemeral(true).queue();
		}
	}
}
