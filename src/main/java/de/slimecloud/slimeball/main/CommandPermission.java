package de.slimecloud.slimeball.main;

import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.condition.ICommandPermission;
import de.mineking.discordutils.commands.context.ICommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CommandPermission implements ICommandPermission<ICommandContext> {
	/**
	 * Befehl kann von jedem ausgeführt werden
	 */
	EVERYONE {
		@Override
		public boolean isPermitted(@NotNull SlimeBot bot, @NotNull Member m) {
			return true;
		}
	},
	/**
	 * Diese Befehle können nur von Teammitgliedern ausgeführt werden, sind aber Standardmäßig für alle Servermitglieder sichtbar.
	 * Es wird überprüft, ob der Nutzer, der den Befehl verwendet die `Team-Rolle` hat.
	 * Falls keine Team-Rolle festgelegt wurde, wird überprüft, ob der Nutzer die `MANAGE_SERVER` Berechtigung hat.
	 * Wenn der Nutzer nicht die nötigen Rechte hat, wird ihm eine Fehlermeldung angezeigt.
	 */
	TEAM {
		@Override
		public boolean isPermitted(@NotNull SlimeBot bot, @NotNull Member m) {
			return bot.loadGuild(m.getGuild()).getTeamRole()
					.map(role -> m.getRoles().contains(role))
					.orElse(m.hasPermission(Permission.MANAGE_SERVER));
		}
	},

	/**
	 * Befehle mit dieser Berechtigung sind nur sichtbar für Server-Mitglieder, die die Berechtigung `MANAGE_ROLES` haben.
	 */
	ROLE_MANAGE {
		@Override
		public boolean isPermitted(@NotNull SlimeBot bot, @NotNull Member m) {
			return m.hasPermission(Permission.MANAGE_ROLES);
		}

		@NotNull
		@Override
		public DefaultMemberPermissions requiredPermissions() {
			return DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES);
		}
	},

	/**
	 * Diese Befehle sind nur sichtbar für Server-Mitglieder, die die Berechtigung `MANAGE_SERVER` haben.
	 */
	ADMINISTRATOR {
		@Override
		public boolean isPermitted(@NotNull SlimeBot bot, @NotNull Member m) {
			return m.hasPermission(Permission.MANAGE_SERVER);
		}


		@NotNull
		@Override
		public DefaultMemberPermissions requiredPermissions() {
			return DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER);
		}
	};

	@Override
	public boolean isPermitted(@NotNull CommandManager<ICommandContext, ?> manager, @NotNull ICommandContext context, @Nullable Member member) {
		return member == null || isPermitted(manager.getManager().getBot(SlimeBot.class), member);
	}

	public abstract boolean isPermitted(@NotNull SlimeBot bot, @NotNull Member m);

	@Override
	public void handleUnpermitted(@NotNull CommandManager<ICommandContext, ?> manager, @NotNull ICommandContext context) {
		context.getEvent().replyEmbeds(new EmbedBuilder()
				.setTitle("\uD83D\uDEAB  Fehlende Berechtigung")
				.setColor(manager.getManager().getBot(SlimeBot.class).getColor(context.getEvent().getGuild()))
				.setDescription("Dieser befehl kann nur von einem Teammitglied ausgeführt werden!")
				.build()
		).setEphemeral(true).queue();
	}
}
