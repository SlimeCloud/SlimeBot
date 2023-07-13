package com.slimebot.commands.report;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@ApplicationCommand(name = "block", description = "Verwaltet vom Report-System ausgeschlossene Nutzer")
public class BlockCommand {
	@ApplicationCommand(name = "add", description = "Fügt einen Nutzer zur Block-Liste hinzu, sodass er keine Meldungen mehr machen kann")
	public static class AddCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "user", description = "Der Nutzer, der blockiert werden soll") User user
		) {
			if(Main.blocklist(event.getGuild().getId()).contains(user.getId())) {
				event.replyEmbeds(
						new EmbedBuilder()
								.setTimestamp(Instant.now())
								.setColor(Main.embedColor(event.getGuild().getId()))
								.setTitle(":exclamation: Error: Already blocked!")
								.setDescription(user.getAsMention() + " ist bereits blockiert")
								.build()
				).setEphemeral(true).queue();
				return;
			}

			List<String> updatedList = Main.blocklist(event.getGuild().getId());
			updatedList.add(user.getId());

			YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");
			try {
				config.load();
				config.set("blocklist", updatedList);
				config.save();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}

			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.embedColor(event.getGuild().getId()))
							.setTitle(":white_check_mark: Erfolgreich Blockiert")
							.setDescription(user.getAsMention() + " wurde blockiert und kann nun keine Reports mehr erstellen")
							.build()
			).queue();
		}
	}

	@ApplicationCommand(name = "remove", description = "Gibt einen Nutzer wieder für Meldungen frei")
	public static class RemoveCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "user", description = "Der Nutzer, der ent-blockiert werden soll") User user
		) {
			if(!Main.blocklist(event.getGuild().getId()).contains(user.getId())) {
				event.replyEmbeds(
						new EmbedBuilder()
								.setTimestamp(Instant.now())
								.setColor(Main.embedColor(event.getGuild().getId()))
								.setTitle(":exclamation: Error: Not Found")
								.setDescription(user.getAsMention() + " konnte nicht in der Blockliste gefunden werden!")
								.build()
				).setEphemeral(true).queue();
				return;
			}

			List<String> updatedList = Main.blocklist(event.getGuild().getId());
			updatedList.remove(user.getId());
			YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");

			try {
				config.load();
				config.set("blocklist", updatedList);
				config.save();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}

			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.embedColor(event.getGuild().getId()))
							.setTitle(":white_check_mark: Entblockt")
							.setDescription(user.getAsMention() + " kann nun wieder Reports erstellen")
							.build()
			).queue();
		}
	}

	@ApplicationCommand(name = "list", description = "Zeigt alle vom Report-System ausgeschlossenen Nutzer")
	public static class ListCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) {
			StringBuilder blockList = new StringBuilder();

			for(String id : Main.blocklist(event.getGuild().getId())) {
				User user = Main.jdaInstance.getUserById(id);

				if(user == null) {
					continue;
				}

				blockList.append(user.getAsMention()).append("\n");
			}

			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.embedColor(event.getGuild().getId()))
							.setTitle("Geblockte User:")
							.setDescription("Folgende Member sind blockiert und können keine Reports mehr erstellen:\n" + blockList)
							.build()
			).queue();
		}
	}
}
