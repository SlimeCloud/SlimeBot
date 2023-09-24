package com.slimebot.events;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.MeetingConfig;
import de.mineking.discord.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MeetingListener extends ListenerAdapter {
	public final static Modal modal = Modal.create("meeting:agenda", "Punkt zur Agenda hinzufügen")
			.addActionRow(TextInput.create("text", "Der Text", TextInputStyle.SHORT).build())
			.build();

	public final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm");

	@Override
	public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
		String[] id = event.getComponentId().split(":", 2);

		if(!id[0].equals("meeting")) return;

		switch (id[1]) {
			case "end" -> {
				event.editComponents().queue();
				sendEmptyMessage(event.getGuild(), Instant.now().plus(2, ChronoUnit.WEEKS));

				//TODO sendTodoMessage
			}
			case "agenda:add" -> event.replyModal(modal).queue();

			case "presence:yes" -> editPresence(event, event.getUser(), 2);
			case "presence:unknown" -> editPresence(event, event.getUser(), 3);
			case "presence:no" -> editPresence(event, event.getUser(), 4);
		}
	}

	@Override
	public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		if(!event.getModalId().equals("meeting:agenda")) return;

		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
		modifyFieldValue(builder, 0, value -> value + (value.length() == 1 ? "" : "\n") + event.getValue("text").getAsString());

		event.editMessageEmbeds(builder.build())
				.setComponents(buildComponents(builder.getFields().get(0).getValue()))
				.queue();
	}

	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		if(!event.getComponentId().equals("meeting:agenda:remove")) return;

		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
		modifyFieldValue(builder, 0, value -> {
			int i = Integer.parseInt(event.getSelectedOptions().get(0).getValue());
			String[] temp = value.split("\n");

			temp[i] = "~~" + temp[i] + "~~";
			return String.join("\n", temp);
		});

		event.editMessageEmbeds(builder.build())
				.setComponents(buildComponents(builder.getFields().get(0).getValue()))
				.queue();
	}

	private void editPresence(ButtonInteractionEvent event, User user, int field) {
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

		for(int i = 2; i <= 4; i++) modifyFieldValue(builder, i, value -> value.replace(user.getAsMention(), ""));
		modifyFieldValue(builder, field, value -> value + (value.length() == 1 ? "" : "\n") + user.getAsMention());

		event.editMessageEmbeds(builder.build()).queue();
	}

	private void modifyFieldValue(EmbedBuilder builder, int field, Function<String, String> handler) {
		MessageEmbed.Field f = builder.getFields().get(field);
		builder.getFields().set(field, new MessageEmbed.Field(
				f.getName(),
				handler.apply(f.getValue()),
				f.isInline()
		));
	}

	public static void sendEmptyMessage(Guild guild, Instant time) {
		GuildConfig.getConfig(guild).getMeetingConfig().flatMap(MeetingConfig::getMeetingChannel).ifPresent(channel ->
				channel.sendMessage(
						new MessageCreateBuilder()
								.setContent(GuildConfig.getConfig(guild).getStaffRole().map(Role::getAsMention).orElse(""))
								.setComponents(buildComponents(""))
								.setEmbeds(
										new EmbedBuilder()
												.setColor(GuildConfig.getColor(guild))
												.setTitle("Team Besprechung **" + formatter.format(Main.atTime(time, 20)) + "**")
												.setThumbnail(guild.getIconUrl())
												.addField(
														"Agenda",
														"",
														false
												)
												.addBlankField(false)
												.addField(
														"Voraussichtlich Anwesend",
														"",
														true
												)
												.addField(
														"Nicht sicher\n" + EmbedBuilder.ZERO_WIDTH_SPACE,
														GuildConfig.getConfig(guild).getStaffRole()
																.map(r -> guild.getMembersWithRoles(r).stream()
																		.map(Member::getAsMention)
																		.collect(Collectors.joining("\n"))
																)
																.orElse(""),
														true
												)
												.addField(
														"Voraussichtlich abwesend",
														"",
														true
												)
												.setTimestamp(Main.atTime(time, 20))
												.build()
								)
								.build()
				).queue()
		);
	}

	private static List<ActionRow> buildComponents(String agenda) {
		return List.of(
				ActionRow.of(
						Button.success("meeting:presence:yes", "Ich kann"),
						Button.secondary("meeting:presence:unknown", "Vielleicht"),
						Button.danger("meeting:presence:no", "Ich kann nicht"),
						Button.primary("meeting:agenda:add", "Agenda-Punkt hinzufügen")
				),
				ActionRow.of(createAgendaSelect(agenda)),
				ActionRow.of(Button.danger("meeting:end", "Meeting beenden"))
		);
	}

	private static StringSelectMenu createAgendaSelect(String agenda) {
		StringSelectMenu.Builder builder = StringSelectMenu.create("meeting:agenda:remove")
				.setPlaceholder("Agenda-Punkt entfernen");

		List<String> temp = Arrays.stream(agenda.split("\n"))
				.filter(a -> a.length() > 1)
				.filter(a -> !a.startsWith("~~"))
				.toList();

		if(temp.isEmpty()) builder.setDisabled(true).addOption("---", "---");
		else {
			for(int i = 0; i < temp.size(); i++)
				builder.addOption(Utils.label(temp.get(i), SelectOption.LABEL_MAX_LENGTH), String.valueOf(i));
		}

		return builder.build();
	}
}
