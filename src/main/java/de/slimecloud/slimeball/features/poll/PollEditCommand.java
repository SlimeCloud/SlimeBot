package de.slimecloud.slimeball.features.poll;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.button.MenuComponent;
import de.mineking.discordutils.ui.components.button.ToggleComponent;
import de.mineking.discordutils.ui.components.select.StringSelectComponent;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.modal.ModalMenu;
import de.mineking.discordutils.ui.modal.TextComponent;
import de.mineking.javautils.database.Where;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
					Map<String, List<String>> options = state.<Map<String, List<String>>>getState("values", LinkedHashMap.class);
					options.put(response.getString("name"), Collections.emptyList());
					state.setState("values", options);

					long id = state.getState("id", long.class);
					bot.getPolls().updateField(Where.equals("id", id), "values", options);

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
						.appendDescription(bot.getPolls().getPoll(s.getState("id", long.class)).map(p -> p.buildChoices(s.getEvent().getGuild())).orElse("*Nicht gefunden*"))
						.build()
				),
				ComponentRow.of(
						new ButtonComponent("max.label", ButtonColor.GRAY, "Maximale Stimmzahl pro Nutzer").asDisabled(true),
						new ButtonComponent("max.subtract", ButtonColor.BLUE, "-").asDisabled(s -> s.getState("max", int.class) <= 1).appendHandler(s -> {
							s.setState("max", int.class, i -> i - 1);
							s.update();
						}),
						new ButtonComponent("max.add", ButtonColor.BLUE, "+").appendHandler(s -> {
							s.setState("max", int.class, i -> i + 1);
							s.update();
						})
				),
				new StringSelectComponent("options.remove", s -> s.<Map<String, List<String>>>getState("values", LinkedHashMap.class).keySet().stream()
						.map(o -> SelectOption.of(o, o))
						.toList()
				).asDisabled(s -> s.getState("values", Map.class).size() <= 2).setPlaceholder("Option entfernen").appendHandler((state, values) -> {
					Map<String, List<String>> options = state.<Map<String, List<String>>>getState("values", LinkedHashMap.class);
					options.remove(values.get(0).getValue());
					state.setState("values", options);

					state.update();
				}),
				ComponentRow.of(
						new MenuComponent<>(addModal, ButtonColor.GRAY, "Option hinzufügen").transfereState(),
						new ToggleComponent("names", e -> e ? ButtonColor.GREEN : ButtonColor.GRAY, "Namen anzeigen")
				)
		).effect((state, name, oldValue, newValue) -> {
			long id = state.getState("id", long.class);

			if (!bot.getPolls().getColumns().containsKey(name)) return;
			bot.getPolls().updateField(Where.equals("id", id), name, newValue);
		});
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull MessageContextInteractionEvent event) {
		bot.getPolls().getPoll(event.getTarget().getIdLong()).ifPresentOrElse(
				poll -> menu.createState()
						.setState("id", poll.getId())
						.setState("names", poll.isNames())
						.setState("max", poll.getMax())
						.setState("values", poll.getValues())
						.display(event),
				() -> event.reply(":x: Abstimmung nicht gefunden!").setEphemeral(true).queue()
		);
	}
}
