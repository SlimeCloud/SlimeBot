package de.slimecloud.slimeball.features.fdmds;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.events.Listener;
import de.mineking.discordutils.events.handlers.ButtonHandler;
import de.mineking.discordutils.events.handlers.ModalHandler;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.features.github.ContributorCommand;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.main.SlimeEmoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
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
	public void sendModal(@NotNull IModalCallback event, @Nullable String question, @Nullable String choices) {
		event.replyModal(Modal.create("fdmds:" + (question == null ? "send" : "edit"), "Schlage eine fdmds Frage vor")
				.addActionRow(TextInput.create("question", "Deine Frage", TextInputStyle.SHORT)
						.setPlaceholder("Welche Eissorte mögt ihr am liebsten?")
						.setValue(question)
						.setMinLength(10)
						.setMaxLength(150)
						.setRequired(true)
						.build()
				)
				.addActionRow(TextInput.create("choices", "Deine Antwortmöglichkeiten", TextInputStyle.PARAGRAPH)
						.setPlaceholder("Jede Antwortmöglichkeit in einer neuen Zeile, z.B:\nErdbeere\nCookie\nSchokolade")
						.setValue(choices)
						.setMinLength(10)
						.setMaxLength(800)
						.setRequired(true)
						.build()
				)
				.build()
		).queue();
	}

	@Listener(type = ModalHandler.class, filter = "fdmds:(.*)")
	public void handleFdmdsModal(@NotNull SlimeBot bot, @NotNull ModalInteractionEvent event) {
		String question = event.getValue("question").getAsString();
		String[] temp = event.getValue("choices").getAsString().split("\n");

		if(event.getModalId().contains("send")) {
			//Call event
			new FdmdsSubmitedEvent(event.getMember(), question).callEvent();
		}

		//Verify input
		if (temp.length <= 1) {
			event.reply("Du musst **mindestens 2** Antwortmöglichkeiten angeben!\n**Achte darauf jede Antwortmöglichkeit in eine neue Zeile zu schreiben!**").setEphemeral(true).queue();
			return;
		}

		if (temp.length > 9) {
			event.reply("Du kannst **maximal 9** Antwortmöglichkeiten angeben!").setEphemeral(true).queue();
			return;
		}

		//Convert to string
		StringBuilder choices = new StringBuilder();
		for (int i = 0; i < temp.length; i++) {
			choices.append(SlimeEmoji.number(i + 1).getEmoji(event.getGuild()).getFormatted())
					.append(" -> ")
					.append(temp[i].strip())
					.append("\n");
		}

		//Build embed
		EmbedBuilder embed = new EmbedBuilder()
				.setColor(bot.getColor(event.getGuild()))
				.setDescription(question)
				.addField("Auswahlmöglichkeiten", choices.toString(), false);

		if (event.getModalId().contains("send")) embed.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl());
		else {
			MessageEmbed.AuthorInfo current = event.getMessage().getEmbeds().get(0).getAuthor();
			embed.setAuthor(current.getName(), current.getUrl(), current.getIconUrl()).setFooter("Bearbeitet von " + event.getUser().getGlobalName());
		}


		//Create message
		MessageEditBuilder message = new MessageEditBuilder()
				.setEmbeds(embed.build())
				.setActionRow(
						Button.secondary("fdmds.edit", "Bearbeiten"),
						Button.danger("fdmds.send", "Senden")
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

	@Listener(type = ButtonHandler.class, filter = "fdmds.edit")
	public void editFdmds(ButtonInteractionEvent event) {
		MessageEmbed embed = event.getMessage().getEmbeds().get(0);
		sendModal(event, embed.getFields().get(0).getValue(), embed.getFields().get(1).getValue().lines()
				.map(s -> s.split(" -> ", 2)[1])
				.collect(Collectors.joining("\n"))
		);
	}

	@Listener(type = ButtonHandler.class, filter = "fdmds.send")
	public void sendFdmds(@NotNull SlimeBot bot, @NotNull CommandManager<?, ?> manager, @NotNull ButtonInteractionEvent event) {
		bot.loadGuild(event.getGuild()).getFdmds().ifPresent(fdmds -> {
			//Load information from embed
			MessageEmbed embed = event.getMessage().getEmbeds().get(0);

			String question = embed.getFields().get(0).getValue();
			String choices = embed.getFields().get(1).getValue();

			if (embed.getAuthor() != null) {
				//Call event
				new FdmdsCreateEvent(ContributorCommand.getUser(embed), event.getMember(), question).callEvent();

				//Create and send embed
				EmbedBuilder builder = new EmbedBuilder()
						.setColor(bot.getColor(event.getGuild()))
						.setAuthor(embed.getAuthor().getName(), embed.getAuthor().getUrl(), embed.getAuthor().getIconUrl())
						.setDescription(question)
						.addField("Auswahlmöglichkeiten", choices, false);

				fdmds.getChannel().sendMessageEmbeds(builder.build())
						.setContent(fdmds.getRole().map(Role::getAsMention).orElse(null))
						.addActionRow(Button.secondary("fdmds:create", "Frage einreichen"))
						.queue(m -> {
							//Add reactions
							for (int i = 0; i < choices.lines().count(); i++) m.addReaction(SlimeEmoji.number(i + 1).getEmoji(event.getGuild())).queue();

							//Create thread
							m.createThreadChannel("Unterhaltet euch über diese Frage!").queue();

							event.reply("Frage verschickt!").setEphemeral(true).queue();
						});
			}

			//TODO: Backward compatibility. This should be removed as soon as enough questions with the new system have been submitted
			else {
				String footerText = embed.getFooter().getText();
				Member requester = event.getGuild().getMemberById(footerText.substring(footerText.lastIndexOf(' ') + 2, footerText.length() - 1));

				StringBuilder text = new StringBuilder()
						.append(fdmds.getRole().map(Role::getAsMention).orElse("")).append("\n")
						.append("Einen Wunderschönen hier ist ").append(requester.getAsMention()).append(" ,\n\n")
						.append(question).append("\n\n")
						.append(choices).append("\n\n")
						.append("Du möchtest selbst eine Umfrage Einreichen? Verwende ")
						.append(manager.getCommand(FdmdsCommand.class).getAsMention(event.getGuild().getIdLong()))
						.append(" oder den Knopf unter dieser Nachricht!");

				//Send, create thread and add reactions
				fdmds.getChannel().sendMessage(text)
						.addActionRow(Button.secondary("fdmds:create", "Frage einreichen"))
						.queue(m -> {
							for (int i = 0; i < choices.lines().count(); i++) m.addReaction(SlimeEmoji.number(i + 1).getEmoji(event.getGuild())).queue();

							m.createThreadChannel("Unterhaltet euch über diese Frage!").queue();

							event.reply("Frage verschickt!").setEphemeral(true).queue();
						});
			}

			event.getMessage().delete().queue();
		});
	}
}
