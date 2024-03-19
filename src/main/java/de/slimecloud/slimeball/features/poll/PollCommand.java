package de.slimecloud.slimeball.features.poll;

import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.commands.option.OptionArray;
import de.mineking.discordutils.commands.option.defaultValue.BooleanDefault;
import de.mineking.discordutils.commands.option.defaultValue.IntegerDefault;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.button.MenuComponent;
import de.mineking.discordutils.ui.components.button.ToggleComponent;
import de.mineking.discordutils.ui.components.button.label.TextLabel;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.modal.ModalMenu;
import de.mineking.javautils.database.Where;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.main.SlimeEmoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationCommand(name = "poll", description = "Erstellt eine Abstimmung")
public class PollCommand {
	public final CommandPermission permission = CommandPermission.TEAM;

	public PollCommand(@NotNull SlimeBot bot, @NotNull DiscordUtils<?> discordUtils) {
		discordUtils.getJDA().addEventListener(new PollListener(bot));
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "Die Frage") String question,
	                           @OptionArray(minCount = 2, maxCount = 5) @Option(name = "choice", description = "Die Auswahlmöglichkeiten") String[] options,
	                           @IntegerDefault(1) @Option(description = "Die maximale Anzahl pro Nutzer", required = false, minValue = 1) int max,
	                           @Option(description = "Rolle, die erwähnt wird", required = false) Role role,
                               @BooleanDefault(false) @Option(description = "Namen der Nutzer anzeigen? (Nur bei internen Abstimmungen!)", required = false) boolean names
	) {
		ReplyCallbackAction action = role == null ? event.deferReply() : event.reply(role.getAsMention());

		//Send defer first to get the id of the message. Will be used as poll id
		action.flatMap(InteractionHook::retrieveOriginal)
				.flatMap(mes -> {
					//Create poll
					Poll poll = bot.getPolls().createPoll(mes.getIdLong(), max, names, options);

					MessageEditAction temp = mes.editMessageEmbeds(new EmbedBuilder()
							.setTitle("\uD83D\uDCCA  Abstimmung")
							.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl())
							.setColor(bot.getColor(event.getGuild()))
							.setDescription(question)
							.addField(
									"Ergebnisse",
									poll.buildChoices(event.getGuild()),
									false
							)
							.setFooter(max == 1 ? null : "Maximale Stimmzahl: " + max)
							.build()
					);

					AtomicInteger i = new AtomicInteger();
					return temp.setActionRow(
							StringSelectMenu.create("poll:select")
									.setPlaceholder("Wähle weise")
									.addOptions(Arrays.stream(options)
											.map(s -> SelectOption.of(StringUtils.abbreviate(s, SelectOption.LABEL_MAX_LENGTH), String.valueOf(i.get()))
													//Number keycap emoji
													.withEmoji(SlimeEmoji.number(i.incrementAndGet()).getEmoji(event.getGuild()))
											)
											.toList()
									)
									.addOptions(SelectOption.of("Auswahl aufheben", "-1").withEmoji(Emoji.fromFormatted("❌")))
									.setMaxValues(max)
									.build()
					);
				})
				.queue();
	}
}