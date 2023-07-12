package com.slimebot.report.commands;

import com.slimebot.main.Checks;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class Blockreport extends ListenerAdapter {
	public static boolean isBlocked(Member member) {
		return Main.database.handle(handle -> handle.createQuery("select count(*) from report_blocks where guild = :guild and \"user\" = :user")
				.bind("guild", member.getGuild().getIdLong())
				.bind("user", member.getIdLong())
				.mapTo(int.class).one()
		) > 0;
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		if(!event.getName().equals("blockreport")) return;

		if(Checks.hasTeamRole(event.getMember())) {
			EmbedBuilder noTeam = new EmbedBuilder()
					.setTimestamp(Instant.now())
					.setColor(Main.database.getColor(event.getGuild()))
					.setTitle(":exclamation: Error")
					.setDescription("Der Befehl kann nur von einem Teammitglied ausgeführt werden!");

			event.replyEmbeds(noTeam.build()).setEphemeral(true).queue();
			return;
		}

		switch(event.getOption("action").getAsString()) {
			case "add" -> {
				if(isBlocked(event.getOption("user").getAsMember())) {
					EmbedBuilder embedBuilder = new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.database.getColor(event.getGuild()))
							.setTitle(":exclamation: Error: Already blocked!")
							.setDescription(event.getOption("user").getAsMember().getAsMention() + " ist bereits blockiert");

					event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
					return;
				}

				Main.database.run(handle -> handle.createUpdate("insert into report_blocks values(:guild, :user)")
						.bind("guild", event.getGuild().getIdLong())
						.bind("user", event.getOption("user").getAsUser().getIdLong())
						.execute()
				);

				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setTimestamp(Instant.now())
						.setColor(Main.database.getColor(event.getGuild()))
						.setTitle(":white_check_mark: Erfolgreich Blockiert")
						.setDescription(event.getOption("user").getAsMentionable().getAsMention() + " wurde blockiert und kann nun keine Reports mehr erstellen");

				event.replyEmbeds(embedBuilder.build()).queue();
			}
			case "remove" -> {
				if(!isBlocked(event.getOption("user").getAsMember())) {
					EmbedBuilder embedBuilder = new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.database.getColor(event.getGuild()))
							.setTitle(":exclamation: Error: Not Found")
							.setDescription(event.getOption("user").getAsMember() + " konnte nicht in der Blockliste gefunden werden!");

					event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
					return;
				}

				Main.database.run(handle -> handle.createUpdate("delete from report_blocks where guild = :guild and \"user\" = :user")
						.bind("guild", event.getGuild().getIdLong())
						.bind("user", event.getOption("user").getAsUser().getIdLong())
						.execute()
				);


				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setTimestamp(Instant.now())
						.setColor(Main.database.getColor(event.getGuild()))
						.setTitle(":white_check_mark: Entblockt")
						.setDescription(event.getOption("user").getAsMentionable().getAsMention() + " kann nun wieder Reports erstellen");

				event.replyEmbeds(embedBuilder.build()).queue();
			}
			case "list" -> {
				StringBuilder msg = new StringBuilder();

				for(long id : Main.database.handle(handle -> handle.createQuery("select user from report_blocks where guild = :guild")
						.mapTo(long.class)
						.list()
				)) {
					Member member = event.getGuild().getMemberById(id);

					if(member == null) continue;

					msg.append(member.getAsMention()).append("\n");
				}

				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setTimestamp(Instant.now())
						.setColor(Main.database.getColor(event.getGuild()))
						.setTitle("Geblockte User:")
						.setDescription("Folgende Member sind blockiert und können keine Reports mehr erstellen:\n" + msg);

				event.replyEmbeds(embedBuilder.build()).queue();
			}
			default -> event.reply(":exclamation: **Error!** \nEtwas ist schief gelaufen bitte kontaktiere einen Netrunner\nDev Info: com.slimebot.report.commands.Blockreport Zeile 85").queue();
		}
	}
}
