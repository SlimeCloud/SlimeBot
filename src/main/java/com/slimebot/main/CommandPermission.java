package com.slimebot.main;

import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.commands.CommandManager;
import de.mineking.discord.commands.ICommandPermission;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public enum CommandPermission implements ICommandPermission {
	/**
	 * Diese Befehle können nur von Teammitgliedern ausgeführt werden, sind aber Standardmäßig für alle Servermitglieder sichtbar.
	 * Es wird überprüft, ob der Nutzer, der den Befehl verwendet die `Team-Rolle` hat.
	 * Falls keine Team-Rolle festgelegt wurde, wird überprüft, ob der Nutzer die `MANAGE_SERVER` Berechtigung hat.
	 * Wenn der Nutzer nicht die nötigen Rechte hat, wird ihm eine Fehlermeldung angezeigt.
	 */
	TEAM {
		@Override
		public boolean isPermitted(Member m) {
			return GuildConfig.getConfig(m.getGuild()).getStaffRole()
					.map(role -> m.getRoles().contains(role))
					.orElse(m.hasPermission(Permission.MANAGE_SERVER));
		}

		@Override
		public boolean isPermitted(CommandManager<?> manager, GenericInteractionCreateEvent event) {
			return isPermitted(event.getMember());
		}

		@Override
		public void handleUnpermitted(CommandManager<?> manager, GenericCommandInteractionEvent event) {
			event.reply("Dieser befehl kann nur von einem Teammitglied ausgeführt werden").setEphemeral(true).queue();
		}
	},
	/**
	 * Befehle mit dieser Berechtigung sind nur sichtbar für Server-Mitglieder, die die Berechtigung `MANAGE_ROLES` haben.
	 */
	ROLE_MANAGE {
		@Override
		public boolean isPermitted(Member m) {
			return m.hasPermission(Permission.MANAGE_ROLES);
		}

		@Override
		public DefaultMemberPermissions requirePermissions() {
			return DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES);
		}
	},
	/**
	 * Diese Befehle sind nur sichtbar für Server-Mitglieder, die die Berechtigung `MANAGE_SERVER` haben.
	 */
	ADMINISTRATOR {
		@Override
		public boolean isPermitted(Member m) {
			return m.hasPermission(Permission.MANAGE_SERVER);
		}

		@Override
		public DefaultMemberPermissions requirePermissions() {
			return DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER);
		}
	};

	public abstract boolean isPermitted(Member m);
}
