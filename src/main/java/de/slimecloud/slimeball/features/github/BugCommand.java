package de.slimecloud.slimeball.features.github;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.cooldown.Cooldown;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.events.Listener;
import de.mineking.discordutils.events.handlers.ModalHandler;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GHIssue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@ApplicationCommand(name = "bug", description = "Melde einen Bug")
public class BugCommand {
	@NotNull
	public static Modal createModal(@Nullable Message target) {
		return Modal.create("bug" + (target == null ? "" : ":" + target.getJumpUrl()), "Melde einen Bug")
				.addActionRow(TextInput.create("title", "Titel", TextInputStyle.SHORT)
						.setMinLength(5)
						.setPlaceholder("Eine kurze präzise Beschreibung des Bugs")
						.build()
				)
				.addActionRow(TextInput.create("reproduction", "Schritte zum Reproduzieren", TextInputStyle.PARAGRAPH)
						.setMinLength(10)
						.setPlaceholder("""
								1. Gehe zu '....'
								2. Klicke auf '....'
								3. Scrolle nach unten zu '....'"""
						)
						.build()
				)
				.addActionRow(TextInput.create("description", "Beschreibung", TextInputStyle.PARAGRAPH)
						.setMinLength(10)
						.setPlaceholder("Eine ausführliche Beschreibung des Bugs")
						.build()
				)
				.addActionRow(TextInput.create("expected", "Erwartetes Verhalten", TextInputStyle.PARAGRAPH)
						.setMinLength(10)
						.setPlaceholder("Ich erwarte, dass '....' passiert")
						.build()
				)
				.addActionRow(TextInput.create("solution", "Lösung", TextInputStyle.PARAGRAPH)
						.setPlaceholder("Ich würde '....' ändern, damit '....' passiert")
						.setRequired(false)
						.build()
				)
				.build();
	}

	@Cooldown(interval = 5, unit = TimeUnit.MINUTES, identifier = "bug")
	public void handleCooldown(@NotNull ICommandContext context) {
		context.getEvent().reply("Du kannst nur alle 5 Minuten einen bug melden!").setEphemeral(true).queue();
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event) {
		event.replyModal(createModal(null)).queue();
	}

	@Listener(type = ModalHandler.class, filter = "bug(:.*)?")
	public void handleModal(@NotNull SlimeBot bot, @NotNull ModalInteractionEvent event) throws IOException {
		String[] id = event.getModalId().split(":", 2);
		String solution = event.getValue("solution").getAsString();

		String text = IOUtils.resourceToString("/github_issue_template.md", StandardCharsets.UTF_8)
				.replace("%description%", event.getValue("description").getAsString())
				.replace("%reproduction%", event.getValue("reproduction").getAsString())
				.replace("%expected%", event.getValue("expected").getAsString())
				.replace("%solution%", solution.isEmpty() ? "N/A" : solution)
				.replace("%context%", id.length == 1 ? "N/A" : id[1]);

		GHIssue issue = bot.getGithub().getApi().getRepository(bot.getConfig().getGithubRepository())
				.createIssue(event.getValue("title").getAsString())
				.body(text + "\n\n" + "Reported by Discord Member " + event.getUser().getName() + " (" + event.getUser().getIdLong() + ")")
				.label("type: bug")
				.create();

		bot.loadGuild(event.getGuild()).getLogChannel().ifPresent(channel -> channel.sendMessageEmbeds(new EmbedBuilder()
				.setTitle("Neuer Bug: **" + event.getValue("title").getAsString() + "**", issue.getHtmlUrl().toString())
				.setColor(bot.getColor(event.getGuild()))
				.setThumbnail(event.getMember().getEffectiveAvatarUrl())

				.setDescription(text.substring(0, Math.min(text.length(), MessageEmbed.DESCRIPTION_MAX_LENGTH)))
				.setFooter("Report von: " + event.getUser().getGlobalName() + " (" + event.getUser().getId() + ")")
				.build()
		).addActionRow(Button.link(issue.getHtmlUrl().toString(), "Auf GitHub ansehen")).queue());

		event.reply("Der Report wurde erfolgreich ausgeführt").setEphemeral(true).queue();
	}
}
