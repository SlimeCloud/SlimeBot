package de.slimecloud.slimeball.features.level.card;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListManager;
import de.mineking.discordutils.ui.components.select.StringSelectComponent;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.features.level.Level;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "rank", description = "Zeigt Level-Informationen zu einer Nutzer an", scope = Scope.GUILD)
public class RankCommand {
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getLevel().isPresent();

	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull CommandManager<ICommandContext, ?> manager, @NotNull ListManager<ICommandContext> list) {
		manager.registerCommand(list.createCommand(
				s -> bot.getLevel(),
				new StringSelectComponent("details", state -> state.<ListContext<Level>>getCache("context").entries().stream()
						.map(l -> SelectOption.of(state.getEvent().getGuild().getMember(l.getUser()).getEffectiveName(), l.getUser().getId())
								.withEmoji(switch (l.getRank()) {
									case 0 -> Emoji.fromFormatted("\uD83E\uDD47");
									case 1 -> Emoji.fromFormatted("\uD83E\uDD48");
									case 2 -> Emoji.fromFormatted("\uD83E\uDD49");
									default -> null;
								})
						)
						.toList()
				).setPlaceholder("Details anzeigen").appendHandler((s, v) -> {
					s.deferUpdate();

					Member member = s.getEvent().getGuild().getMemberById(v.get(0).getValue());
					s.getEvent().getHook().editOriginalAttachments(
							bot.getCardProfiles().getProfile(member).getData()
									.render(member)
									.getFile()
					).setEmbeds().setComponents().queue();
				})
		).withName("leaderboard").withDescription("Zeigt das aktuelle Level-Leaderboard"));
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(name = "target", description = "Der Nutzer, dessen Rank angezeigt werden soll", required = false) Member target
	) {
		if (target != null && target.getUser().isBot()) {
			event.reply(":x: Bots können nicht leveln!").setEphemeral(true).queue();
			return;
		}

		event.deferReply().queue();

		event.getHook().editOriginalAttachments(
				bot.getCardProfiles().getProfile(target != null ? target : event.getMember()).getData()
						.render(target != null ? target : event.getMember())
						.getFile()
		).queue();
	}
}
