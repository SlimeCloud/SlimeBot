package de.slimecloud.slimeball.features.fdmds;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.events.Listener;
import de.mineking.discordutils.events.handlers.ButtonHandler;
import de.mineking.discordutils.events.handlers.ModalHandler;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.main.SlimeEmoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

@ApplicationCommand(name = "fdmds", description = "Schlage eine Frage für \"Frag doch mal den Schleim\" vor!", scope = Scope.GUILD)
public class FdmdsCommand {
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getFdmds().isPresent();

	@ApplicationCommandMethod
	@Listener(type = ButtonHandler.class, filter = "fdmds:create")
	//Parameters without @Option (or special meaning) will have a null value when called by the CommandManager
	public void sendModal(@NotNull IModalCallback event, @Nullable String title, @Nullable String question, @Nullable String choices) {
		event.replyModal(Modal.create("fdmds:" + (question == null ? "send" : "edit"), "Schlage eine FdmdS Frage vor")
				.addActionRow(TextInput.create("title", "Titel", TextInputStyle.SHORT)
						.setPlaceholder("Eissorten")
						.setValue(title)
						.setMinLength(10)
						.setMaxLength(ThreadChannel.MAX_NAME_LENGTH)
						.setRequired(true)
						.build()
				)
				.addActionRow(TextInput.create("question", "Deine Frage", TextInputStyle.PARAGRAPH)
						.setPlaceholder("Welche Eissorte mögt ihr am liebsten?")
						.setValue(question)
						.setMinLength(10)
						.setMaxLength(163)
						.setRequired(true)
						.build()
				)
				.addActionRow(TextInput.create("choices", "Deine Antwortmöglichkeiten", TextInputStyle.PARAGRAPH)
						.setPlaceholder("Jede Antwortmöglichkeit in einer neuen Zeile, z.B:\nErdbeere\nCookie\nSchokolade")
						.setValue(choices)
						.setMinLength(5)
						.setMaxLength(800)
						.setRequired(true)
						.build()
				)
				.build()
		).queue();
	}

	@Listener(type = ModalHandler.class, filter = "fdmds:(send|edit)")
	public void handleFdmdsModal(@NotNull SlimeBot bot, @NotNull ModalInteractionEvent event) {
		String title = event.getValue("title").getAsString();
		String question = event.getValue("question").getAsString();
		String[] temp = event.getValue("choices").getAsString().split("\n");

		if (event.getModalId().contains("send")) {
			//Call event
			new FdmdsSubmitedEvent(event.getMember(), question).callEvent();
		}

		//Verify input
		if (temp.length <= 1) {
			event.reply("Du musst **mindestens 2** Antwortmöglichkeiten angeben!\n**Achte darauf jede Antwortmöglichkeit in eine neue Zeile zu schreiben!**").setEphemeral(true).queue();
			return;
		}

		if (temp.length > MessagePoll.MAX_ANSWERS) {
			event.reply("Du kannst **maximal 10** Antwortmöglichkeiten angeben!").setEphemeral(true).queue();
			return;
		}

		//Convert to string
		StringBuilder choices = new StringBuilder();
		for (int i = 0; i < temp.length; i++) {
			if (temp[i].length() >= MessagePoll.MAX_ANSWER_TEXT_LENGTH) {
				event.reply("Eine Antwort darf **maximal " + MessagePoll.MAX_ANSWER_TEXT_LENGTH + "** Zeichen lang sein!").setEphemeral(true).queue();
				return;
			}

			choices.append(SlimeEmoji.number(i).getEmoji(event.getGuild()).getFormatted())
					.append(" -> ")
					.append(temp[i].strip())
					.append("\n");
		}

		//Build embed
		EmbedBuilder embed = new EmbedBuilder()
				.setTitle(title)
				.setColor(bot.getColor(event.getGuild()))
				.setDescription(question)
				.addField("Auswahlmöglichkeiten", choices.toString(), false);

		if (event.getModalId().contains("send")) embed.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl()).setFooter("Nutzer ID: " + event.getMember().getId());
		else {
			MessageEmbed current = event.getMessage().getEmbeds().get(0);
			embed.setAuthor(current.getAuthor().getName(), current.getAuthor().getUrl(), current.getAuthor().getIconUrl()).setFooter(current.getFooter().getText());
		}


		//Create message
		MessageEditBuilder message = new MessageEditBuilder()
				.setEmbeds(embed.build())
				.setActionRow(
						Button.secondary("fdmds:edit", "Bearbeiten"),
						Button.primary("fdmds:add", "Hinzufügen")
				);

		//Edit or send
		if (event.getModalId().contains("edit")) {
			event.getMessage().editMessage(message.build()).queue();
			event.reply("Frage wurde bearbeitet.").setEphemeral(true).queue();
		} else bot.loadGuild(event.getGuild()).getFdmds().map(FdmdsConfig::getLogChannel).ifPresent(channel -> {
			channel.sendMessage(MessageCreateData.fromEditData(message.build())).queue(mes -> {
				mes.addReaction(SlimeEmoji.UP.getEmoji(mes.getGuild())).queue();
				mes.addReaction(SlimeEmoji.DOWN.getEmoji(mes.getGuild())).queue();
			});
			event.reply("Frage erfolgreich eingereicht! Das Team wird die Frage kontrollieren und anschließend veröffentlicht.").setEphemeral(true).queue();
		});
	}

	@Listener(type = ButtonHandler.class, filter = "fdmds:edit")
	public void editFdmds(ButtonInteractionEvent event) {
		MessageEmbed embed = event.getMessage().getEmbeds().get(0);
		sendModal(event, embed.getTitle(), embed.getDescription(), embed.getFields().get(0).getValue().lines()
				.map(s -> s.split(" -> ", 2)[1])
				.collect(Collectors.joining("\n"))
		);
	}

	@Listener(type = ButtonHandler.class, filter = "fdmds:add")
	public void addFdmds(@NotNull SlimeBot bot, @NotNull ButtonInteractionEvent event) {
		bot.getFdmdsQueue().addItemToQueue(event.getMessage());
		event.editComponents(ActionRow.of(
				Button.secondary("fdmds:edit", "Bearbeiten"),
				Button.danger("fdmds:remove", "Entfernen")
		)).queue();

		event.getHook().sendMessage("Umfrage zu Queue hinzugefügt").setEphemeral(true).queue();
	}

	@Listener(type = ButtonHandler.class, filter = "fdmds:remove")
	public void removeFdmds(@NotNull SlimeBot bot, @NotNull ButtonInteractionEvent event) {
		bot.getFdmdsQueue().removeItemFromQueue(event.getMessageIdLong());
		event.editComponents(ActionRow.of(
				Button.secondary("fdmds:edit", "Bearbeiten"),
				Button.primary("fdmds:add", "Hinzufügen")
		)).queue();

		event.getHook().sendMessage("Umfrage aus Queue entfernt").setEphemeral(true).queue();
	}
}
