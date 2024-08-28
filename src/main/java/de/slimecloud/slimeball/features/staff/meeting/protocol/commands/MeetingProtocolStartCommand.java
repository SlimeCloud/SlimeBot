package de.slimecloud.slimeball.features.staff.meeting.protocol.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "protocol-start", description = "Startet das Automatische Meeting Protokoll", defer = true)
public class MeetingProtocolStartCommand {

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
		if (bot.getMeetingProtocol().getReceiver () != null) event.getHook().sendMessage("stop meeting recording first").queue();
		GuildVoiceState state = event.getMember().getVoiceState();
		if (state != null) {
			AudioChannelUnion union = state.getChannel();
			if (union != null) {
				bot.getMeetingProtocol().start(union.asVoiceChannel());
				event.getHook().sendMessage("meeting recording started").queue();
			} else event.getHook().sendMessage("connect first to a voice channel").queue();
		} else event.getHook().sendMessage("connect first to a voice channel").queue();
	}

}
