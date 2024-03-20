package de.slimecloud.slimeball.features.poll;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.button.MenuComponent;
import de.mineking.discordutils.ui.components.select.StringSelectComponent;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.modal.ModalMenu;
import de.mineking.discordutils.ui.modal.TextComponent;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationCommand(name = "Abstimmung bearbeiten", type = Command.Type.MESSAGE)
public class PollEditCommand {
	public final CommandPermission permission = CommandPermission.ROLE_MANAGE; //This makes this command only visible for team members

	private MessageMenu menu;
	private final ModalMenu addModal;

	public PollEditCommand(@NotNull SlimeBot bot, @NotNull UIManager manager) {
		addModal = manager.createModal(
				"poll.options.add",
				s -> "Option hinzufügen",
				List.of(
						new TextComponent("name", "Option", TextInputStyle.SHORT)
								.setPlaceholder("Ja / Nein")
								.setMaxLength(90)
				),
				(state, response) -> {
					long id = state.getState("id", long.class);
					bot.getPolls().getPoll(id).ifPresent(poll -> {
						poll.getValues().put(response.getString("name"), Collections.emptyList());
						poll.update();
					});

					menu.createState(state).display(state.getEvent());
				}
		);

		menu = manager.createMenu(
				"poll.edit",
				MessageRenderer.embed(s -> new EmbedBuilder()
						.setColor(bot.getColor(s.getEvent().getGuild()))
						.setTitle("Umfrage bearbeiten")
						.setDescription("https://discord.com/channels/" + s.getEvent().getGuild().getId() + "/" + s.getEvent().getChannel().getId() + "/" + s.getState("id", String.class) + " ")
						.appendDescription("Umfrage wird aktualisiert sobald der nächste Nutzer eine Auswahl tritt")
						.appendDescription(s.<Optional<Poll>>getCache("poll").map(p -> p.buildChoices(s.getEvent().getGuild())).orElse("*Nicht gefunden*"))
						.build()
				),
				ComponentRow.of(
						new ButtonComponent("max.label", ButtonColor.GRAY, "Maximale Stimmzahl pro Nutzer").asDisabled(true),
						new ButtonComponent("max.subtract", ButtonColor.BLUE, "-").asDisabled(s -> s.<Optional<Poll>>getCache("poll").map(Poll::getMax).map(m -> m <= 1).orElse(true)).appendHandler(s -> {
							s.<Optional<Poll>>getCache("poll").ifPresent(poll -> poll.setMax(poll.getMax() - 1).update());
							s.update();
						}),
						new ButtonComponent("max.add", ButtonColor.BLUE, "+").appendHandler(s -> {
							s.<Optional<Poll>>getCache("poll").ifPresent(poll -> poll.setMax(poll.getMax() + 1).update());
							s.update();
						})
				),
				new StringSelectComponent("options.remove", s -> s.<Optional<Poll>>getCache("poll").map(p -> p.getValues().keySet()).orElse(Collections.emptySet()).stream()
						.map(o -> SelectOption.of(o, o))
						.toList()
				).asDisabled(s -> s.<Optional<Poll>>getCache("poll").map(p -> p.getValues().size()).map(m -> m <= 1).orElse(true)).setPlaceholder("Option entfernen").appendHandler((state, values) -> {
					state.<Optional<Poll>>getCache("poll").ifPresent(poll -> {
						poll.getValues().remove(values.get(0).getValue());
						poll.update();
					});
					state.update();
				}),
				ComponentRow.of(
						new MenuComponent<>(addModal, ButtonColor.GRAY, "Option hinzufügen").transfereState(),
						new ButtonComponent("names", s -> s.<Optional<Poll>>getCache("poll").filter(Poll::isNames).map(p -> ButtonColor.GREEN).orElse(ButtonColor.GRAY), "Namen anzeigen").appendHandler(s -> {
							s.<Optional<Poll>>getCache("poll").ifPresent(poll -> poll.setNames(!poll.isNames()).update());
							s.update();
						})
				)
		).cache(state -> {
			long id = state.getState("id", long.class);
			state.setCache("poll", bot.getPolls().getPoll(id));
		});
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull MessageContextInteractionEvent event) {
		bot.getPolls().getPoll(event.getTarget().getIdLong()).ifPresentOrElse(
				poll -> menu.createState()
						.setState("id", poll.getId())
						.display(event),
				() -> event.reply(":x: Abstimmung nicht gefunden!").setEphemeral(true).queue()
		);
	}
}
