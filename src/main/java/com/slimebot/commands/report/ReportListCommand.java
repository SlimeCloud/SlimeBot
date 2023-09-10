package com.slimebot.commands.report;

import com.slimebot.main.CommandContext;
import com.slimebot.main.CommandPermission;
import com.slimebot.report.Filter;
import com.slimebot.report.Report;
import com.slimebot.report.list.ReportSet;
import de.mineking.discord.commands.choice.Choice;
import de.mineking.discord.commands.inherited.Option;
import de.mineking.discord.list.ListCommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Arrays;
import java.util.Collections;

public class ReportListCommand extends ListCommand<CommandContext, Report, ReportSet> {
	public ReportListCommand() {
		super(CommandPermission.TEAM,
				(context, options) -> new ReportSet(context.guild, Filter.valueOf(options.get("status").toUpperCase())),
				Collections.singletonList(new Option(OptionType.STRING, "status", "Der Status, nach dem gefiltert werden soll")
						.required()
						.choices(Arrays.asList(
								new Choice("Alle", "all"),
								new Choice("Geschlossen", "closed"),
								new Choice("Offen", "open")
						))
				)
		);

		description = "Zeigt alle Meldungen an";
	}
}
