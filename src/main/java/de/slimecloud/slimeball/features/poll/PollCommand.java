package de.slimecloud.slimeball.features.poll;

import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.commands.option.OptionArray;
import de.mineking.discordutils.commands.option.defaultValue.BooleanDefault;
import de.mineking.discordutils.commands.option.defaultValue.IntegerDefault;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "poll", description = "Erstellt eine Abstimmung")
public class PollCommand {
	public final CommandPermission permission = CommandPermission.TEAM;

	public PollCommand(@NotNull SlimeBot bot, @NotNull DiscordUtils<?> discordUtils) {
		discordUtils.getJDA().addEventListener(new PollListener(bot));
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "Die Frage") String question,
	                           @OptionArray(minCount = 2, maxCount = 5) @Option(name = "choice", description = "Die Auswahlmöglichkeiten", maxLength = 90) String[] options,
	                           @IntegerDefault(1) @Option(description = "Die maximale Anzahl pro Nutzer", required = false, minValue = 1, maxValue = 25) int max,
	                           @Option(description = "Rolle, die erwähnt wird", required = false) Role role,
	                           @BooleanDefault(false) @Option(description = "Namen der Nutzer anzeigen? (Nur bei internen Abstimmungen!)", required = false) boolean names
	) {
		ReplyCallbackAction action = role == null ? event.deferReply() : event.reply(role.getAsMention());

		//Send defer first to get the id of the message. Will be used as poll id
		action.flatMap(InteractionHook::retrieveOriginal)
				.flatMap(mes -> {
					//Create poll
					Poll poll = bot.getPolls().createPoll(mes.getIdLong(), max, names, options);

					return mes.editMessageEmbeds(new EmbedBuilder()
							.setTitle("\uD83D\uDCCA  Abstimmung")
							.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl())
							.setColor(bot.getColor(event.getGuild()))
							.setDescription(question + "\n")
							.appendDescription(
									"### Ergebnisse\n\n" +
											poll.buildChoices(event.getGuild())
							)
							.setFooter(max == 1 ? null : "Maximale Stimmzahl: " + max)
							.build()
					).setActionRow(poll.buildMenu(event.getGuild()));
				})
				.queue();
	}
}