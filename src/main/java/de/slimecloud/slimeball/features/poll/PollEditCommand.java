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
import de.mineking.discordutils.ui.components.button.label.TextLabel;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.modal.ModalMenu;
import de.mineking.discordutils.ui.modal.TextComponent;
import de.mineking.javautils.database.Where;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ApplicationCommand(name = "Abstimmung bearbeiten", type = Command.Type.MESSAGE)
public class PollEditCommand {
	public final CommandPermission permission = CommandPermission.ROLE_MANAGE; //This makes this command only visible for team members

	private final MessageMenu menu;

	public PollEditCommand(@NotNull SlimeBot bot, @NotNull UIManager manager) {
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
				ComponentRow.of(
						new ToggleComponent("names", e -> e ? ButtonColor.GREEN : ButtonColor.GRAY, "Namen anzeigen")
				)
		).effect((state, name, oldValue, newValue) -> {
			if (!bot.getPolls().getColumns().containsKey(name)) return;
			bot.getPolls().updateField(Where.equals("id", state.getState("id", long.class)), name, newValue);
		});
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull MessageContextInteractionEvent event) {
		bot.getPolls().getPoll(event.getTarget().getIdLong()).ifPresentOrElse(
				poll -> menu.createState()
						.setState("id", poll.getId())
						.setState("names", poll.isNames())
						.setState("max", poll.getMax())
						.display(event),
				() -> event.reply(":x: Abstimmung nicht gefunden!").setEphemeral(true).queue()
		);
	}
}
