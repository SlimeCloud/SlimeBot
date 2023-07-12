package com.slimebot.report.buttons;

import com.slimebot.report.assets.Report;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DetailDropdown extends ListenerAdapter {
	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		if(!event.getComponentId().equals("report:details")) return;

		int id = Integer.parseInt(event.getValues().get(0));

		Report.get(event.getGuild(), id)
				.ifPresent(report -> event.reply(report.buildMessage()).queue());
	}
}
