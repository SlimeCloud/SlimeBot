package com.slimebot.report.list;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.report.Filter;
import com.slimebot.report.Report;
import de.mineking.discord.list.ListContext;
import de.mineking.discord.list.Listable;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ReportSet implements Listable<Report> {
	private final Guild guild;
	private final Filter filter;

	@Override
	public List<Report> getEntries(IReplyCallback event) {
		return Main.database.handle(handle -> handle.createQuery("select * from reports where guild = :guild")
				.bind("guild", guild.getIdLong())
				.mapTo(Report.class)
				.stream()
				.filter(filter.getFilter())
				.toList()
		);
	}

	@Override
	public EmbedBuilder createEmbed(ListContext<Report> context) {
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(GuildConfig.getColor(guild))
				.setTimestamp(Instant.now())
				.setTitle("Reports mit Filter **" + filter.toString() + "**");

		if (context.entries.isEmpty()) {
			builder.setDescription("*Keine Einträge*");
		} else {
			builder
					.setFooter("Insgesamt " + context.entries.size() + " Reports, die dem Filter entsprechen")
					.setDescription("Nutze </report details:" + context.manager.getCommandCache().getGlobalCommand("report") + "> oder das Dropdown menu um mehr infos zu einem Report zu bekommen.\n");
		}

		return builder;
	}

	@Override
	public List<ActionRow> getComponents(ListContext<Report> context) {
		List<ActionRow> components = new ArrayList<>(Listable.super.getComponents(context));

		if (!context.entries.isEmpty()) {
			List<SelectOption> options = new ArrayList<>();

			for (int i = ((context.page - 1) * entriesPerPage()); i < (context.page * entriesPerPage()) && i < context.entries.size(); i++) {
				Report report = context.entries.get(i);

				options.add(SelectOption.of("Report #" + report.getId(), String.valueOf(report.getId()))
						.withDescription("Details zum Report #" + report.getId())
				);
			}

			components.add(ActionRow.of(
					StringSelectMenu.create("report:details")
							.setPlaceholder("Details zu einem Report")
							.addOptions(options)
							.build()
			));
		}

		return components;
	}
}
