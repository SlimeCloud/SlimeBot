package de.slimecloud.slimeball.features.quote;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.cooldown.CooldownIncrement;
import de.mineking.discordutils.commands.condition.cooldown.CooldownPool;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

@CooldownPool("quote")
@ApplicationCommand(name = "Nachricht Zitieren", type = Command.Type.MESSAGE, defer = true)
public class QuoteMessageCommand {
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getQuoteChannel().isPresent();

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull MessageContextInteractionEvent event, @CooldownIncrement Runnable cooldown) {
		QuoteCommand.quote(bot, event,
				event.getTarget().getMember(),
				event.getTarget().getContentRaw(),
				event.getTarget(),
				event.getTarget().getTimeCreated(),
				cooldown
		);
	}
}
