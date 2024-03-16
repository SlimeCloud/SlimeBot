package de.slimecloud.slimeball.features.level.card.badge;

import de.mineking.discordutils.commands.AnnotatedCommand;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.IAutocompleteContext;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Autocomplete;
import de.mineking.discordutils.commands.option.AutocompleteOption;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.list.ListManager;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

@ApplicationCommand(name = "badge", description = "Verwaltet RankCard-Badges", scope = Scope.GUILD)
public class BadgeCommand {
	public final CommandPermission permission = CommandPermission.TEAM;
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getLevel().isPresent();

	public static void handleAutocomplete(@NotNull SlimeBot bot, @NotNull CommandAutoCompleteInteractionEvent event) {
		event.replyChoices(
				CardBadgeData.getBadges(bot).stream()
						.filter(d -> d.contains(event.getFocusedOption().getValue()))
						.map(d -> new Command.Choice(d, d))
						.toList()
		).queue();
	}

	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull ListManager<ICommandContext> manager, @NotNull AnnotatedCommand<?, ICommandContext, ?> cmd) {
		cmd.addSubcommand(manager.createCommand(state -> bot.getCardBadges()).withDescription("Zeigt alle Badges an"));

		cmd.addSubcommand(manager.createCommand(
				(ctx, state) -> state.setState("badge", ctx.getEvent().getOption("badge").getAsString()),
				state -> bot.getCardBadges()
		).withName("list_owners").withDescription("Zeigt alle Mitglieder und Rollen mit einem Badge").addOption(new AutocompleteOption<>(OptionType.STRING, "badge", "Das Badge, dessen Besitzer angezeigt werden", true) {
			@Override
			public void handleAutocomplete(@NotNull IAutocompleteContext context) {
				BadgeCommand.handleAutocomplete(bot, context.getEvent());
			}
		}));

		cmd.addSubcommand(manager.createCommand(
				(ctx, state) -> {
					IMentionable target = ctx.getEvent().getOption("target").getAsMentionable();
					state.setState(target instanceof Role ? "role" : "user", target.getIdLong());
				},
				state -> bot.getCardBadges()
		).withName("list_badges").withDescription("Zeigt alle Badges einer Rolle / eines Mitglieds").addOption(new OptionData(OptionType.MENTIONABLE, "target", "Die Rolle / das Mitglied", true)));
	}

	@ApplicationCommand(name = "grant", description = "Gibt einem Mitglied / einer Rolle ein Badge")
	public static class GrantCommand {
		@Autocomplete("badge")
		public void handleAutocomplete(@NotNull SlimeBot bot, @NotNull CommandAutoCompleteInteractionEvent event) {
			BadgeCommand.handleAutocomplete(bot, event);
		}

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Das Mitglied / die Rolle, dem das Badge gegeben wird") IMentionable target,
		                           @Option(description = "Das Badge, die dem Mitglied / der Rolle gegeben wird") String badge
		) {
			//Check if badge exists
			if (!CardBadgeData.getBadge(bot, badge).exists()) {
				event.reply(":x: Badge nicht gefunden!").setEphemeral(true).queue();
				return;
			}

			bot.getCardBadges().grant(target, badge);

			event.reply("Badge vergeben!").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "grant_default", description = "Einer Rolle ihr icon als Badge")
	public static class GrantDefaultCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Die Rolle, dem das Badge gegeben wird") Role target
		) {
			bot.getCardBadges().grant(target, target.getId());
			event.reply("Badge vergeben!").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "revoke", description = "Entzieht einem Mitglied / einer Rolle ein Badge")
	public static class RevokeCommand {
		@Autocomplete("badge")
		public void handleAutocomplete(@NotNull SlimeBot bot, @NotNull CommandAutoCompleteInteractionEvent event) {
			BadgeCommand.handleAutocomplete(bot, event);
		}

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Das Mitglied / die Rolle, dem das Badge entzogen wird") IMentionable target,
		                           @Option(description = "Das BAdge, die dem Mitglied / der Rolle entzogen wird") String badge
		) {
			//Check if badge exists
			if (!CardBadgeData.getBadge(bot, badge).exists()) {
				event.reply(":x: Badge nicht gefunden!").setEphemeral(true).queue();
				return;
			}

			bot.getCardBadges().revoke(target, badge);

			event.reply("Badge entzogen!").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "revoke_default", description = "Einer Rolle ihr icon als Badge")
	public static class RevokeDefaultCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Die Rolle, dem das Badge gegeben wird") Role target
		) {
			bot.getCardBadges().revoke(target, target.getId());
			event.reply("Badge entzogen!").setEphemeral(true).queue();
		}
	}
}
