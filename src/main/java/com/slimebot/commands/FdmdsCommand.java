package com.slimebot.commands;

import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import com.slimebot.main.SlimeEmoji;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.events.interaction.ButtonHandler;
import de.mineking.discord.events.interaction.ModalHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Optional;

@ApplicationCommand(name = "fdmds", description = "Schlage eine Frage für \"Frag doch mal den Schleim\" vor!", feature = "fdmds")
public class FdmdsCommand {
	@ApplicationCommandMethod
	public void sendModal(IModalCallback event, String question, String choices) { //Non-@Option parameters will be null when called from DiscordUtils CommandHandler
		event.replyModal(
				Modal.create("fdmds:" + (question == null ? "send" : "edit"), "Schlage eine fdmds Frage vor")
						.addActionRow(
								TextInput.create("question", "Deine Frage", TextInputStyle.SHORT)
										.setPlaceholder("Welche Eissorte mögt ihr am liebsten?")
										.setValue(question)
										.setMinLength(10)
										.setMaxLength(150)
										.setRequired(true)
										.build()
						)
						.addActionRow(
								TextInput.create("choices", "Deine Antwortmöglichkeiten", TextInputStyle.PARAGRAPH)
										.setPlaceholder("Antworten mit ; trennen z.B. Erdbeere; Cookie; Schokolade")
										.setValue(choices)
										.setMinLength(10)
										.setMaxLength(800)
										.setRequired(true)
										.build()
						)
						.build()
		).queue();
	}

	@WhenFinished
	public void setup(DiscordUtils manager) {
		manager.getEventManager().registerHandler(new ModalHandler("fdmds:(.*)", event -> {
			String question = event.getValue("question").getAsString();

			StringBuilder choicesStr = new StringBuilder();

			if(event.getModalId().contains("send")) {
				String[] choices = event.getValue("choices").getAsString().split(";");

				if(choices.length <= 1) {
					event.reply("Du musst mindestens 2 Antwortmöglichkeiten angeben!").setEphemeral(true).queue();
					return;
				}
				if(choices.length > 9) {
					event.reply("Du kannst maximal 9 Antwortmöglichkeiten angeben!").setEphemeral(true).queue();
					return;
				}

				for(int i = 0; i < choices.length; i++) {
					choicesStr
							.append(SlimeEmoji.fromId(i + 1).format())
							.append(" -> ")
							.append(choices[i].strip())
							.append("\r\n");
				}
			}

			else {
				choicesStr.append(event.getValue("choices").getAsString());
			}

			MessageEditData message = new MessageEditBuilder()
					.setActionRow(
							Button.secondary("fdmds.edit", "Bearbeiten"),
							Button.danger("fdmds.send", "Senden")
					)
					.setEmbeds(
							new EmbedBuilder()
									.setColor(Main.database.getColor(event.getGuild()))
									.setTitle("Frag doch mal den Schleim")
									.setFooter("Vorschlag von: " + event.getUser().getGlobalName() + " (" + event.getUser().getId() + ")")
									.addField("Frage:", "Heute würde ich gerne von euch wissen, " + question, false)
									.addField("Auswahlmöglichkeiten:", choicesStr.toString(), false)
									.build()
					)
					.build();

			if(event.getMessage() != null) {
				event.getMessage().editMessage(message).queue();

				event.reply("Frage wurde bearbeitet.").setEphemeral(true).queue();
			}

			else {
				Main.database.getChannel(event.getGuild(), DatabaseField.FDMDS_LOG_CHANNEL).ifPresentOrElse(
						channel -> {
							channel.sendMessage(MessageCreateData.fromEditData(message)).queue();
							event.reply("Vorschlag erfolgreich verschickt!").setEphemeral(true).queue();
						},
						() -> event.reply("Error: Channel wurde nicht gesetzt!").setEphemeral(true).queue()
				);
			}
		}));

		manager.getEventManager().registerHandler(new ButtonHandler("fdmds.edit", event -> {
			MessageEmbed embed = event.getMessage().getEmbeds().get(0);
			sendModal(event, embed.getFields().get(0).getValue(), embed.getFields().get(1).getValue());
		}));
		manager.getEventManager().registerHandler(new ButtonHandler("fdmds.send", event -> {
			Main.database.getChannel(event.getGuild(), DatabaseField.FDMDS_CHANNEL).ifPresentOrElse(
					channel -> {

						MessageEmbed embed = event.getMessage().getEmbeds().get(0);
						String question = embed.getFields().get(0).getValue();
						String choices = embed.getFields().get(1).getValue();


						StringBuilder text = new StringBuilder()
								.append("Einen Wunderschönen <:slimewave:1080225151104331817>,\n\n")
								.append(question).append("\n\n")
								.append(choices).append("\n\n");

						Optional<Role> role = Main.database.getRole(event.getGuild(), DatabaseField.FDMDS_ROLE);

						role.ifPresent(value -> text.append(value.getAsMention()));

						// Send and add reactions
						channel.sendMessage(text).queue(m -> {
							for(int i = 0; i < choices.lines().count(); i++) {
								m.addReaction(SlimeEmoji.fromId(i + 1).emoji).queue();
							}

							event.reply("Frage verschickt!").setEphemeral(true).queue();
						});

						event.getMessage().delete().queue();
					},
					() -> event.reply("Error: Channel wurde nicht gesetzt!").setEphemeral(true).queue()
			);
		}));
	}
}
