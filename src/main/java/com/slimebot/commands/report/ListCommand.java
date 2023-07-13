package com.slimebot.commands.report;

import com.slimebot.main.Main;
import com.slimebot.report.Filter;
import com.slimebot.report.Report;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.Choice;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@ApplicationCommand(name = "list", description = "Zeigt alle Meldungen an")
public class ListCommand {
	public final List<Choice> statusChoices = Arrays.asList(
			new Choice("Alle", "all"),
			new Choice("Geschlossen", "closed"),
			new Choice("Offen", "open")
	);

	@ApplicationCommandMethod
	public void performCommand(DiscordUtils manager, SlashCommandInteractionEvent event,
	                           @Option(name = "status", description = "Der Status, nach dem gefiltert werden soll", choices = "statusChoices") String status
	) {
		Filter filter = Filter.valueOf(status.toUpperCase());

		EmbedBuilder embed = new EmbedBuilder()
				.setTimestamp(Instant.now())
				.setDescription("Nutze </report details:" + event.getCommandId() + "> oder das Dropdown menu um mehr infos zu einem Report zu bekommen.")
				.setColor(Main.database.getColor(event.getGuild()));

		List<Report> reports = Main.database.handle(handle -> handle.createQuery("select * from reports where guild = :guild")
				.bind("guild", event.getGuild().getIdLong())
				.mapTo(Report.class)
				.stream()
				.filter(filter.filter)
				.toList()
		);

		if(reports.isEmpty()) {
			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.database.getColor(event.getGuild()))
							.setTitle(":exclamation: Error: No Reports Found")
							.setDescription("Es wurden keine Reports zu der Ausgewählten option (" + status + ") gefunden!")
							.build()
			).setEphemeral(true).queue();
		}

		StringSelectMenu.Builder select = StringSelectMenu.create("report:details")
				.setPlaceholder("Details zu einem Report")
				.setMaxValues(1);

		for(int i = 0; i < reports.size() && i < 24; i++) {
			Report report = reports.get(i);

			embed.addField("Report #" + report.getId(),
					report.shortDescription(),
					false
			);

			select.addOption("Report #" + report.getId(), String.valueOf(report.getId()), "Details zum Report #" + report.getId());
		}

		if(reports.size() > 24) {
			embed.setFooter("Weitere Reports gefunden, es können jedoch maximal 25 angezeigt werden");
		}

		event.replyEmbeds(embed.build()).addActionRow(select.build()).setEphemeral(true).queue();
	}
}
