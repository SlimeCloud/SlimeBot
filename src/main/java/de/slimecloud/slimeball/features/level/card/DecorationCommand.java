package de.slimecloud.slimeball.features.level.card;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Autocomplete;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

@ApplicationCommand(name = "decoration", description = "Verwaltet RankCard-Dekorationen", scope = Scope.GUILD)
public class DecorationCommand {
	public final CommandPermission permission = CommandPermission.TEAM;
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getLevel().isPresent();

	@ApplicationCommand(name = "grant", description = "Gibt einem mitglied eine Dekoration")
	public static class GrantCommand {
		@Autocomplete("decoration")
		public void handleAutocomplete(@NotNull SlimeBot bot, @NotNull CommandAutoCompleteInteractionEvent event) {
			event.replyChoices(
					Arrays.stream(new File(bot.getConfig().getLevel().get().getDecorationFolder()).list())
							.filter(d -> d.contains(event.getFocusedOption().getValue()))
							.map(d -> new Command.Choice(d, d))
							.toList()
			).queue();
		}

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Das Mitglied, dem die Dekoration gegeben wird") Member target,
		                           @Option(description = "Die Dekoration, die dem Mitglied gegeben wird") String decoration
		) {
			//Check if decoration exists
			if (!new File(bot.getConfig().getLevel().get().getDecorationFolder(), decoration).exists()) {
				event.reply(":x: Dekoration nicht gefunden!").setEphemeral(true).queue();
				return;
			}

			bot.getCardDecorations().grantDecoration(target, decoration);

			event.reply("Dekoration vergeben!").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "revoke", description = "Entzieht einem mitglied eine Dekoration")
	public static class RevokeCommand {
		@Autocomplete("decoration")
		public void handleAutocomplete(@NotNull SlimeBot bot, @NotNull CommandAutoCompleteInteractionEvent event) {
			event.replyChoices(
					Arrays.stream(new File(bot.getConfig().getLevel().get().getDecorationFolder()).list())
							.filter(d -> d.contains(event.getFocusedOption().getValue()))
							.map(d -> new Command.Choice(d, d))
							.toList()
			).queue();
		}

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Das Mitglied, dem die Dekoration entzogen wird") Member target,
		                           @Option(description = "Die Dekoration, die dem Mitglied entzogen wird") String decoration
		) {
			//Check if decoration exists
			if (!new File(bot.getConfig().getLevel().get().getDecorationFolder(), decoration).exists()) {
				event.reply(":x: Dekoration nicht gefunden!").setEphemeral(true).queue();
				return;
			}

			bot.getCardDecorations().revokeDecoration(target, decoration);

			event.reply("Dekoration entzogen!").setEphemeral(true).queue();
		}
	}
}
