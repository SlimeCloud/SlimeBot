package com.slimebot.commands;

import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

@ApplicationCommand(name = "Bug Melden", guildOnly = true, type = Command.Type.MESSAGE)
public class BugContextCommand {
	@ApplicationCommandMethod
	public void performCommand(MessageContextInteractionEvent event) {
		if(BugCommand.timeout.containsKey(event.getUser().getIdLong())) {
			if(BugCommand.timeout.get(event.getUser().getIdLong()) > System.currentTimeMillis()) {
				event.reply("Du kannst nur alle 5 Minuten einen Bug Melden!").setEphemeral(true).queue();
				return;
			}
		}

		event.replyModal(BugCommand.createModal(event.getTarget())).queue();
	}
}
