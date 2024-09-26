package de.slimecloud.slimeball.features.staff.meeting.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.list.ListManager;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.features.staff.meeting.protocol.commands.MeetingProtocolStartCommand;
import de.slimecloud.slimeball.features.staff.meeting.protocol.commands.MeetingProtocolStopCommand;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "meeting", description = "Verwalte Team Meetings", scope = Scope.GUILD)
public class MeetingCommand {

	public final CommandPermission permission = CommandPermission.TEAM;
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getMeeting().isPresent();

	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull Command<ICommandContext> command, @NotNull ListManager<ICommandContext> manager) {
		command.addSubcommand(MeetingProtocolStartCommand.class);
		command.addSubcommand(MeetingProtocolStopCommand.class);
	}

}
