package de.slimecloud.slimeball.features.general;


import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.Result;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

@ApplicationCommand(name = "role_check", description = "Geht ALLE Mitglieder durch und gibt ihnen eine Rolle", scope = Scope.GUILD_GLOBAL, defer = true)
public class BulkAddRoleCommand {
	public final CommandPermission permission = CommandPermission.ROLE_MANAGE;

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "Auf welche Rolle sollen die User überprüft werden?") Role role,
	                           @Option(description = "Sollen Bots mit überprüft werden?") boolean bots
	) {
		//Filter member sto affect
		List<Member> members = event.getGuild().getMembers().stream()
				.filter(m -> !m.getUser().isBot() || bots)
				.filter(m -> !m.getRoles().contains(role))
				.toList();

		//Compress to one RestAction and execute
		RestAction.allOf(members.stream().map(m -> event.getGuild().addRoleToMember(m, role).mapToResult()).toList())
				//Send confirmation containing information about success / failure
				.flatMap(result -> event.getHook().editOriginalEmbeds(new EmbedBuilder()
						.setTitle(":white_check_mark: Rollen Verteilt")
						.setColor(bot.getColor(event.getGuild()))
						.setDescription("Die Rolle " + role.getAsMention() + " wurde " + result.stream().filter(Result::isSuccess).count() + " Membern gegeben! (" + result.stream().filter(Result::isFailure).count() + " fehlgeschlagen)")
						.setTimestamp(Instant.now())
						.build()
				))
				.queue();
	}
}
