package com.slimebot.commands.report;

import com.slimebot.main.Main;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;

@ApplicationCommand(name = "block", description = "Verwaltet vom Report-System ausgeschlossene Nutzer")
public class BlockCommand {
	public static boolean isBlocked(Member member) {
		return Main.database.handle(handle -> handle.createQuery("select count(*) from report_blocks where guild = :guild and \"user\" = :user")
				.bind("guild", member.getGuild().getIdLong())
				.bind("user", member.getIdLong())
				.mapTo(int.class).one()
		) > 0;
	}

	@ApplicationCommand(name = "add", description = "Fügt einen Nutzer zur Block-Liste hinzu, sodass er keine Meldungen mehr machen kann")
	public static class AddCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "user", description = "Der Nutzer, der blockiert werden soll") Member user
		) {
			if(isBlocked(user)) {
				event.replyEmbeds(
						new EmbedBuilder()
								.setTimestamp(Instant.now())
								.setColor(Main.database.getColor(event.getGuild()))
								.setTitle(":exclamation: Error: Already blocked!")
								.setDescription(user.getAsMention() + " ist bereits blockiert")
								.build()
				).setEphemeral(true).queue();
				return;
			}

			Main.database.run(handle -> handle.createUpdate("insert into report_blocks values(:guild, :user)")
					.bind("guild", event.getGuild().getIdLong())
					.bind("user", user.getIdLong())
					.execute()
			);

			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.database.getColor(event.getGuild()))
							.setTitle(":white_check_mark: Erfolgreich Blockiert")
							.setDescription(user.getAsMention() + " wurde blockiert und kann nun keine Reports mehr erstellen")
							.build()
			).setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "remove", description = "Gibt einen Nutzer wieder für Meldungen frei")
	public static class RemoveCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "user", description = "Der Nutzer, der ent-blockiert werden soll") Member user
		) {
			if(!isBlocked(user)) {
				event.replyEmbeds(
						new EmbedBuilder()
								.setTimestamp(Instant.now())
								.setColor(Main.database.getColor(event.getGuild()))
								.setTitle(":exclamation: Error: Not Found")
								.setDescription(user.getAsMention() + " konnte nicht in der Blockliste gefunden werden!")
								.build()
				).setEphemeral(true).queue();
				return;
			}

			Main.database.run(handle -> handle.createUpdate("delete from report_blocks where guild = :guild and \"user\" = :user")
					.bind("guild", event.getGuild().getIdLong())
					.bind("user", user.getIdLong())
					.execute()
			);

			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.database.getColor(event.getGuild()))
							.setTitle(":white_check_mark: Entblockt")
							.setDescription(user.getAsMention() + " kann nun wieder Reports erstellen")
							.build()
			).setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "list", description = "Zeigt alle vom Report-System ausgeschlossenen Nutzer")
	public static class ListCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) {
			StringBuilder msg = new StringBuilder();

			for(long id : Main.database.handle(handle -> handle.createQuery("select \"user\" from report_blocks where guild = :guild")
					.bind("guild", event.getGuild().getIdLong())
					.mapTo(long.class)
					.list()
			)) {
				Member member = event.getGuild().getMemberById(id);

				if(member == null) continue;

				msg.append(member.getAsMention()).append("\n");
			}

			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.database.getColor(event.getGuild()))
							.setTitle("Geblockte User:")
							.setDescription("Folgende Member sind blockiert und können keine Reports mehr erstellen:\n" + msg)
							.build()
			).setEphemeral(true).queue();
		}
	}
}
