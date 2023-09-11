package com.slimebot.commands;

import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

@ApplicationCommand(name = "Nachricht Zitieren", type = Command.Type.MESSAGE, feature = "quote")
public class QuoteMessageCommand {
	@ApplicationCommandMethod
	public void performCommand(MessageContextInteractionEvent event) {
		QuoteCommand.quote(event,
				event.getTarget().getMember(),
				event.getTarget().getContentRaw(),
				event.getTarget().getJumpUrl(),
				event.getTarget().getTimeCreated()
		);
	}
}
