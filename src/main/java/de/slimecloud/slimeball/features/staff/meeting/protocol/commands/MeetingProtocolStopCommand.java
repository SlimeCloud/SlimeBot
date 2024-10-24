package de.slimecloud.slimeball.features.staff.meeting.protocol.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "protocol-start", description = "Startet das Automatische Meeting Protokoll", defer = true)
public class MeetingProtocolStopCommand {

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
		if (bot.getMeetingProtocol().getReceiver() == null) event.getHook().sendMessage("start meeting recording first").queue();
		bot.getMeetingProtocol().stop(event.getGuild());
		event.getHook().sendMessage("meeting recording stopped!").queue();
	}

}
