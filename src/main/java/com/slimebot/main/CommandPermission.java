package com.slimebot.main;

import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.commands.CommandManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public enum CommandPermission implements de.mineking.discord.commands.CommandPermission {
	TEAM {
		@Override
		public boolean isPermitted(CommandManager<?> manager, GenericInteractionCreateEvent event) {
			return GuildConfig.getConfig(event.getGuild()).getStaffRole()
					.map(role -> event.getMember().getRoles().contains(role))
					.orElse(event.getMember().hasPermission(Permission.MANAGE_SERVER));
		}

		@Override
		public void handleUnpermitted(CommandManager<?> manager, GenericCommandInteractionEvent event) {
			event.reply("Dieser befehl kann nur von einem Teammitglied ausgeführt werden").setEphemeral(true).queue();
		}
	},
	ROLE_MANAGE {
		@Override
		public DefaultMemberPermissions requirePermissions() {
			return DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES);
		}
	},
	ADMINISTRATOR {
		@Override
		public DefaultMemberPermissions requirePermissions() {
			return DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER);
		}
	}
}
