package de.slimecloud.slimeball.features.report.commands;

import de.cyklon.jevent.JEvent;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.events.Listener;
import de.mineking.discordutils.events.handlers.ButtonHandler;
import de.mineking.discordutils.events.handlers.ModalHandler;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListManager;
import de.mineking.discordutils.ui.components.select.StringSelectComponent;
import de.slimecloud.slimeball.features.report.Filter;
import de.slimecloud.slimeball.features.report.Report;
import de.slimecloud.slimeball.features.report.ReportListener;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Arrays;

@ApplicationCommand(name = "report", description = "Verwaltet reports", scope = Scope.GUILD_GLOBAL)
public class ReportCommand {
	public final CommandPermission permission = CommandPermission.TEAM;

	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull Command<ICommandContext> command, @NotNull ListManager<ICommandContext> list) {
		JEvent.getManager().registerListener(new ReportListener(bot));

		//Add subcommands
		command.addSubcommand(BlockCommand.class);
		command.addSubcommand(DetailsCommand.class);

		//Register list command to show all reports
		command.addSubcommand(list.createCommand(
				(ctx, state) -> state.setState("filter", ctx.getEvent().getOption("filter").getAsString()), //Set initial state
				state -> bot.getReports(),
				new StringSelectComponent("details", state -> state.<ListContext<Report>>getCache("context").entries().stream()
						.map(r -> SelectOption.of("#" + r.getId() + ": " + StringUtils.abbreviate(r.getMessage(), SelectOption.LABEL_MAX_LENGTH - 5), String.valueOf(r.getId()))
								.withDescription(r.isOpen() ? null : StringUtils.abbreviate(r.getCloseReason(), SelectOption.DESCRIPTION_MAX_LENGTH))
								.withEmoji(Emoji.fromFormatted(r.getStatus().getEmoji()))
						)
						.toList()
				).appendHandler((state, values) -> {
					state.update(); //Update to reset selection
					state.sendReply(bot.getReports().getReport(Integer.parseInt(values.get(0).getValue())).get().buildMessage("Details zu Report")); //Send details message
				}).setPlaceholder("Details anzeigen")
		).withDescription("Zeigt alle Reports an").addOption(new OptionData(OptionType.STRING, "filter", "Ein Filter der angibt, welche Reports angezeigt werden sollen", true).addChoices(
				Arrays.stream(Filter.values())
						.map(f -> new Choice(f.getName(), f.name()))
						.toList()
		)));
	}

	@Listener(type = ButtonHandler.class, filter = "report:close:(\\d+)")
	public void handleCloseButton(@NotNull ButtonInteractionEvent event) {
		String reportID = event.getComponentId().split(":")[2];

		event.replyModal(Modal.create("report:close:" + reportID, "Meldung schließen")
				.addActionRow(TextInput.create("reason", "Wie Wurde mit dem Report Verfahren?", TextInputStyle.SHORT)
						.setPlaceholder("z. B. Warn, Kick, Mute, Ban, Nichts etc..")
						.setRequired(true)
						.build()
				)
				.build()
		).queue();
	}

	@Listener(type = ModalHandler.class, filter = "report:close:(\\d+)")
	public void handleCloseModal(@NotNull SlimeBot bot, @NotNull ModalInteractionEvent event) {
		try {
			bot.getReports().getReport(Integer.parseInt(event.getModalId().split(":")[2])).ifPresentOrElse(
					report -> {
						report.close(event.getValue("reason").getAsString());

						event.editMessage(MessageEditData.fromCreateData(report.buildMessage("Report geschlossen"))).queue();
						event.getHook().sendMessageEmbeds(
								new EmbedBuilder()
										.setColor(bot.getColor(event.getGuild()))
										.setTimestamp(Instant.now())
										.setTitle("Report **#" + report.getId() + "** geschlossen")
										.setDescription("Der Report **#" + report.getId() + "** erfolgreich geschlossen")
										.build()
						).setEphemeral(true).queue();
					},
					() -> event.reply("Report nicht gefunden!").setEphemeral(true).queue()
			);
		} catch (NumberFormatException e) {
			event.reply("Ungültige Report-ID").setEphemeral(true).queue();
		}
	}
}
