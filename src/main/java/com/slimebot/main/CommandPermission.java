package com.slimebot.main;

import com.slimebot.utils.Checks;
import de.mineking.discord.commands.CommandManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public enum CommandPermission implements de.mineking.discord.commands.CommandPermission {
	TEAM {
		@Override
		public boolean isPermitted(CommandManager<?> manager, GenericInteractionCreateEvent event) {
			return !Checks.hasTeamRole(event.getMember(), event.getGuild());
		}

		@Override
		public void handleUnpermitted(CommandManager<?> manager, GenericCommandInteractionEvent event) {
			event.reply("Dieser befehl kann nur von einem Teammitglied ausgeführt werden").setEphemeral(true).queue();
		}
	},
	ADMINISTRATOR {
		@Override
		public DefaultMemberPermissions requirePermissions() {
			return DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER);
		}
	}
}
