package com.slimebot.report.buttons;

import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class DetailDropdown extends ListenerAdapter {
	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		if(!event.getComponentId().equals("detail_btn")) return;

		String id = event.getValues().get(0);

		Report report = Report.get(event.getGuild().getId(), Integer.valueOf(id));

		MessageEmbed embed = report.asEmbed(event.getGuild().getId());

		if(report.status == Status.CLOSED) {
			event.replyEmbeds(embed).queue();
		}

		else {
			Button closeBtn = Report.closeButton(report.id);
			event.replyEmbeds(embed).setActionRow(closeBtn).queue();
		}
	}
}
