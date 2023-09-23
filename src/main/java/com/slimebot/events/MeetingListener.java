package com.slimebot.events;

import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.MeetingConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class MeetingListener extends ListenerAdapter {
	public final static Modal modal = Modal.create("meeting:agenda", "Punkt zur Agenda hinzufügen")
			.addActionRow(TextInput.create("text", "Der Text", TextInputStyle.SHORT).build())
			.build();

	@Override
	public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
		String[] id = event.getComponentId().split(":", 2);

		if(!id[0].equals("meeting")) return;

		switch (id[1]) {
			case "end" -> {
				event.editComponents().queue();
				sendEmptyMessage(event.getGuild());

				//TODO sendTodoMessage
			}
			case "agenda:add" -> event.replyModal(modal).queue();

			case "presence:yes" -> editPresence(event, event.getUser(), 1);
			case "presence:maybe" -> editPresence(event, event.getUser(), 2);
			case "presence:no" -> editPresence(event, event.getUser(), 3);
		}
	}

	@Override
	public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		if(!event.getModalId().equals("meeting:agenda")) return;

		modifyFieldValue(event.getMessage().getEmbeds().get(0), 0, value -> value + "\n- " + event.getValue("text").getAsString());
	}

	private void editPresence(ButtonInteractionEvent event, User user, int field) {
		MessageEmbed embed = event.getMessage().getEmbeds().get(0);
		EmbedBuilder builder = new EmbedBuilder(embed);

		for(int i = 1; i <= 3; i++) modifyFieldValue(embed, field, value -> value.replaceAll("- " + user.getAsMention() + "(\n|$)", ""));
		modifyFieldValue(embed, field, value -> value + "\n- " + user.getAsMention());

		event.editMessageEmbeds(builder.build()).queue();
	}

	private void modifyFieldValue(MessageEmbed builder, int field, Function<String, String> handler) {
		MessageEmbed.Field f = builder.getFields().get(field);
		builder.getFields().set(field, new MessageEmbed.Field(
				f.getName(),
				handler.apply(f.getValue()),
				true
		));
	}

	public static void sendEmptyMessage(Guild guild) {
		GuildConfig.getConfig(guild).getMeetingConfig().flatMap(MeetingConfig::getMeetingChannel).ifPresent(channel ->
				channel.sendMessageEmbeds(
						new EmbedBuilder()
								.setColor(GuildConfig.getColor(guild))
								.setTitle("Team Besprechung")
								.addField(
										"Agenda",
										"",
										false
								)
								.addField(
										"Voraussichtlich anwesend",
										"",
										false
								)
								.addField(
										"Unbekannt/Vielleicht",
										"",
										true
								)
								.addField(
										"Voraussichtlich abwesend",
										"",
										true
								)
								.build()
				).setContent(GuildConfig.getConfig(guild).getStaffRole().map(Role::getAsMention).orElse("")).queue()
		);
	}
}
