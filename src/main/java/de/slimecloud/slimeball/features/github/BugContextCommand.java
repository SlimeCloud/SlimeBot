package de.slimecloud.slimeball.features.github;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.cooldown.CooldownPool;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

@CooldownPool("bug")
@ApplicationCommand(name = "Bug Melden", type = Command.Type.MESSAGE)
public class BugContextCommand {
	@ApplicationCommandMethod
	public void performCommand(@NotNull MessageContextInteractionEvent event) {
		event.replyModal(BugCommand.createModal(event.getTarget())).queue();
	}
}
