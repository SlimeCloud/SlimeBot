package com.slimebot.commands;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.events.interaction.ButtonHandler;
import de.mineking.discord.events.interaction.ModalHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;

@ApplicationCommand(name = "bug", description = "Melde einen Bug")
public class BugCommand {
	public final static Modal modal = Modal.create("bug", "Melde einen Bug")
			.addActionRow(
					TextInput.create("text", "Bug", TextInputStyle.PARAGRAPH)
							.setMinLength(10)
							.build()
			)
			.build();

	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event) {
		event.replyModal(modal).queue();
	}

	@WhenFinished
	public void setup(DiscordUtils manager) {
		manager.getEventManager().registerHandler(new ModalHandler("bug", event -> {
			YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");

			try {
				config.load();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}

			event.reply("Der Report wurde erfolgreich ausgeführt").setEphemeral(true).queue();

			event.getGuild()
					.getChannelById(MessageChannel.class, config.getString("logChannel"))
					.sendMessageEmbeds(
							new EmbedBuilder()
									.setColor(Main.embedColor(event.getGuild().getId()))
									.setTitle("Ein neuer Bug wurde gefunden!")

									.setDescription("Fehlerbeschreibung: \n\n")
									.appendDescription(event.getValue("text").getAsString() + "\n")
									.setFooter("Report von: " + event.getUser().getGlobalName() + " (" + event.getUser().getId() + ")")
									.build()
					)
					.setActionRow(Button.secondary("bug:close", "Bug schließen")).queue();
		}));

		manager.getEventManager().registerHandler(new ButtonHandler("bug:close", event -> {
			event.getMessage().delete().queue();
			event.reply("Der Bug wurde erfolgreich geschlossen!").setEphemeral(true).queue();
		}));
	}
}
