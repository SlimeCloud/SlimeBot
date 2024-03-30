package de.slimecloud.slimeball.features.staff;

import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class TeamMeeting extends ListenerAdapter {
	private final static Modal agendaModal = Modal.create("meeting:agenda_add", "Agendapunkt hinzufügen")
			.addActionRow(TextInput.create("description", "Beschreibung", TextInputStyle.PARAGRAPH).build())
			.build();

	private final SlimeBot bot;

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event) {
		bot.loadGuild(event.getGuild()).getMeeting().ifPresent(MeetingConfig::setupNotification);
	}

	@Override
	public synchronized void onMessageDelete(@NotNull MessageDeleteEvent event) {
		if (!event.isFromGuild()) return;

		GuildConfig guildConfig = bot.loadGuild(event.getGuild());
		guildConfig.getMeeting().ifPresent(config -> {
			if (config.getMessage() == event.getMessageIdLong()) {
				config.disable(event.getGuild());
				guildConfig.setMeeting(null);

				guildConfig.save();
			}
		});
	}

	@Override
	public synchronized void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
		String[] id = event.getComponentId().split(":");
		if (!id[0].equals("meeting")) return;

		GuildConfig guildConfig = bot.loadGuild(event.getGuild());
		guildConfig.getMeeting().ifPresent(config -> {
			if (event.getMessage().getIdLong() != config.getMessage()) return;

			MessageEmbed current = event.getMessage().getEmbeds().get(0);

			switch (id[1]) {
				case "yes" -> event.editMessage(config.buildMessage(event.getGuild(), current.getTimestamp().toInstant(), current, (y, m, n, a) -> {
					String mention = event.getUser().getAsMention();
					y.add(mention);
					m.remove(mention);
					n.remove(mention);
				})).queue();

				case "late" -> event.editMessage(config.buildMessage(event.getGuild(), current.getTimestamp().toInstant(), current, (y, m, n, a) -> {
					String mention = event.getUser().getAsMention();
					y.remove(mention);
					m.add(mention);
					n.remove(mention);
				})).queue();

				case "no" -> event.editMessage(config.buildMessage(event.getGuild(), current.getTimestamp().toInstant(), current, (y, m, n, a) -> {
					String mention = event.getUser().getAsMention();
					y.remove(mention);
					m.remove(mention);
					n.add(mention);
				})).queue();

				case "agenda" -> event.replyModal(agendaModal).queue();

				case "end" -> {
					//Remove components from current meeting
					event.editComponents().queue();

					createTodos(config, extractAgenda(event.getMessage().getEmbeds().get(0)))
							.flatMap(issues -> {
								EmbedBuilder embed = new EmbedBuilder()
										.setTitle("\uD83D\uDCDC  Besprechung beendet")
										.setColor(bot.getColor(event.getGuild()))
										.setDescription("Team-Besprechung erfolgreich beendet");

								if (!issues.isEmpty()) {
									embed.appendDescription("\n\n### ToDo's\n");
									issues.forEach(i -> embed.appendDescription("- [" + i.getTitle() + "](" + i.getHtmlUrl() + ")\n"));
								}

								return event.getMessage().replyEmbeds(embed.build());
							}).onErrorFlatMap(e -> event.getMessage().replyEmbeds(new EmbedBuilder()
									.setTitle("\uD83D\uDCDC  Besprechung beendet")
									.setColor(bot.getColor(event.getGuild()))
									.setDescription("Team-Besprechung erfolgreich beendet")
									.appendDescription("\n\nFehler beim erstellen der ToDo's")
									.build()
							)).complete(); //We have to use complete to ensure the order of messages. We can not use the queue callback because of the complete usage in createNewMeeting

					//Delete event
					event.getGuild().retrieveScheduledEventById(config.getEvent()).flatMap(ScheduledEvent::delete).queue();

					//This will send a new message
					config.createNewMeeting(current.getTimestamp().toInstant().plus(Duration.ofDays(14)));
					guildConfig.save();
				}
			}
		});
	}

	@Override
	public synchronized void onStringSelectInteraction(StringSelectInteractionEvent event) {
		String[] id = event.getComponentId().split(":");
		if (!id[0].equals("meeting")) return;

		GuildConfig guildConfig = bot.loadGuild(event.getGuild());
		guildConfig.getMeeting().ifPresent(config -> {
			if (event.getMessage().getIdLong() != config.getMessage()) return;

			MessageEmbed current = event.getMessage().getEmbeds().get(0);
			int i = Integer.valueOf(event.getSelectedOptions().get(0).getValue());

			switch (id[1]) {
				case "agenda_remove" -> event.editMessage(config.buildMessage(
						event.getGuild(),
						current.getTimestamp().toInstant(),
						current,
						(y, m, n, a) -> a.remove(i)
				)).queue();

				case "agenda_edit" -> event.replyModal(Modal.create("meeting:agenda_edit:" + i, "Agendapunkt bearbeiten")
						.addActionRow(TextInput.create("description", "Beschreibung", TextInputStyle.PARAGRAPH)
								.setValue(extractAgenda(current).get(i).split(": ", 2)[1])
								.build()
						)
						.build()
				).queue();
			}
		});
	}

	@Override
	public synchronized void onModalInteraction(ModalInteractionEvent event) {
		String[] id = event.getModalId().split(":");
		if (!id[0].equals("meeting")) return;

		GuildConfig guildConfig = bot.loadGuild(event.getGuild());
		guildConfig.getMeeting().ifPresent(config -> {
			if (event.getMessage().getIdLong() != config.getMessage()) return;

			MessageEmbed current = event.getMessage().getEmbeds().get(0);

			switch (id[1]) {
				case "agenda_add" -> event.editMessage(config.buildMessage(
						event.getGuild(),
						current.getTimestamp().toInstant(),
						current,
						(y, m, n, a) -> a.add(event.getUser().getAsMention() + ": " + event.getValue("description").getAsString().replace("\n", " "))
				)).queue();

				case "agenda_edit" -> event.editMessage(config.buildMessage(
						event.getGuild(),
						current.getTimestamp().toInstant(),
						current,
						(y, m, n, a) -> a.set(Integer.parseInt(id[2]), event.getUser().getAsMention() + ": " + event.getValue("description").getAsString().replace("\n", " "))
				)).queue();
			}
		});
	}

	@NotNull
	public RestAction<List<GHIssue>> createTodos(@NotNull MeetingConfig config, @NotNull List<String> entries) {
		if (entries.isEmpty() || bot.getGithub() == null || config.getRepository() == null) return bot.wrap(Collections.emptyList());

		//Get repository and project
		try {
			GHRepository repository = bot.getGithub().getApi().getRepository(config.getRepository());

			//Project id
			return bot.getGithub().execute("""
							query {
								repository(owner: "%owner%", name: "%name%") {
									projectsV2(first: 1) {
										nodes {
											id
										}
									}
								}
							}
							""".replace("%owner%", repository.getOwnerName()).replace("%name%", repository.getName()),
					//Extract id from response
					data -> data
							.getAsJsonObject("repository")
							.getAsJsonObject("projectsV2")
							.getAsJsonArray("nodes").get(0).getAsJsonObject()
							.getAsJsonPrimitive("id").getAsString()
			).flatMap(id -> RestAction.allOf(entries.stream()
					.map(e -> {
						try {
							GHIssue issue = repository.createIssue(e.split(": ", 2)[1]).create();

							return bot.getGithub().execute("""
											mutation {
												addProjectV2ItemById(input: {
													projectId: "%project%"
													contentId: "%issue%"
												}) { clientMutationId }
											}
											""".replace("%project%", id).replace("%issue%", issue.getNodeId()),
									null
							).mapToResult().map(x -> issue);
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
					})
					.toList()
			));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	public static List<String> extractAgenda(@NotNull MessageEmbed embed) {
		return extract(embed.getFields().get(0), e -> e.split("\\. ", 2)[1]);
	}

	@NotNull
	public static List<String> extract(@NotNull MessageEmbed.Field field, @NotNull Function<String, String> handler) {
		if (field.getValue().length() <= 1) return Collections.emptyList();

		return Arrays.stream(field.getValue().split("\n"))
				.filter(s -> !s.isEmpty())
				.map(handler)
				.toList();
	}
}
