package de.slimecloud.slimeball.features.staff;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.main.SlimeEmoji;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Getter
public class MeetingConfig extends ConfigCategory {
	@ConfigField(name = "Kanal", command = "channel", description = "Kanal, in dem Team-Meetings organisiert werden", type = ConfigFieldType.MESSAGE_CHANNEL, required = true)
	private Long channel;

	@ConfigField(name = "Sprachkanal", command = "voice", description = "Der Sprachkanal, in dem Team-Meetings stattfinden", type = ConfigFieldType.VOICE_CHANNEL, required = true)
	private Long voice;

	@Getter
	private Long message;
	@Getter
	private Long event;

	@Getter
	private Long nextMeeting;

	@ConfigField(name = "Repository", command = "repository", description = "Die GitHub Repository, in der ToDo's erstellt werden", type = ConfigFieldType.STRING)
	private String repository;

	private final transient Set<Future<?>> futures = new HashSet<>();

	@Override
	public void enable() {
		createNewMeeting(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).atTime(20, 0).toInstant(SlimeBot.timezone));
	}

	public void setupNotification() {
		futures.forEach(f -> f.cancel(false));
		futures.clear();

		scheduleNotification(Duration.ofHours(1).toMillis());
		scheduleNotification(Duration.ofMinutes(5).toMillis());
	}

	private void scheduleNotification(long delta) {
		long time = nextMeeting - delta - System.currentTimeMillis();
		if (time > 0) futures.add(bot.getExecutor().schedule(() -> getChannel().ifPresent(channel ->
				channel.sendMessage(bot.loadGuild(channel.getGuild()).getTeamRole().map(Role::getAsMention).orElse("@Team") + ", " + TimeFormat.RELATIVE.format(nextMeeting) + " geht das Team-Meeting los!")
						.setMessageReference(message)
						.queue()
		), time, TimeUnit.MILLISECONDS));
	}

	public void createNewMeeting(@NotNull Instant timestamp) {
		nextMeeting = timestamp.toEpochMilli();
		getChannel().ifPresent(channel -> {
			//Send and save message
			Message message = channel
					.sendMessage(MessageCreateData.fromEditData(buildMessage(channel.getGuild(), timestamp, null, null)))
					.complete();
			this.message = message.getIdLong();

			//Create event
			this.event = channel.getGuild().createScheduledEvent("Teamsitzung", getVoiceChannel().orElseThrow(), timestamp.atOffset(SlimeBot.timezone))
					.setDescription("Weitere Informationen: " + message.getJumpUrl())
					.complete().getIdLong();
		});

		setupNotification();
	}

	@Override
	public void disable() {
		if (message == null) return;
		getChannel().ifPresent(channel -> {
			channel.deleteMessageById(message).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
			channel.getGuild().retrieveScheduledEventById(event).flatMap(ScheduledEvent::delete).queue(null, new ErrorHandler().ignore(ErrorResponse.SCHEDULED_EVENT));
		});

		futures.forEach(f -> f.cancel(false));
		futures.clear();
	}

	@NotNull
	public Optional<GuildMessageChannel> getChannel() {
		return Optional.ofNullable(channel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}

	@NotNull
	public Optional<GuildChannel> getVoiceChannel() {
		return Optional.ofNullable(voice).map(id -> bot.getJda().getChannelById(GuildChannel.class, id));
	}

	@NotNull
	public MessageEditData buildMessage(@NotNull Guild guild, @NotNull Instant timestamp, @Nullable MessageEmbed current, @Nullable MeetingHandler handler) {
		List<String> y = new ArrayList<>();
		List<String> m = new ArrayList<>();
		List<String> n = new ArrayList<>();

		List<String> a = new ArrayList<>();

		if (current != null) {
			//Load member presence
			y.addAll(TeamMeeting.extract(current.getFields().get(1), s -> s));
			m.addAll(TeamMeeting.extract(current.getFields().get(2), s -> s));
			n.addAll(TeamMeeting.extract(current.getFields().get(3), s -> s));

			//Load agenda
			a.addAll(TeamMeeting.extractAgenda(current));
		} else bot.loadGuild(guild).getTeamRole().ifPresent(role -> m.addAll(guild.getMembersWithRoles(role).stream()
				.map(Member::getAsMention)
				.toList()
		));

		if (handler != null) handler.handle(y, m, n, a);

		//Build agenda
		List<SelectOption> options = new ArrayList<>();
		StringBuilder agenda = new StringBuilder();

		for (int i = 0; i < a.size(); i++) {
			String entry = a.get(i);

			agenda.append(i + 1).append(". ").append(entry).append("\n");
			options.add(SelectOption.of(entry.split(": ", 2)[1], String.valueOf(i))
					.withEmoji(SlimeEmoji.number((i % 9) + 1).getEmoji(guild))
			);
		}

		boolean empty = options.isEmpty();
		if (empty) options.add(SelectOption.of("---", "---"));

		//Create message
		return new MessageEditBuilder()
				.setEmbeds(new EmbedBuilder()
						.setTitle("\uD83D\uDCAC  Team Besprechung")
						.setColor(bot.getColor(guild))
						.setThumbnail(guild.getIconUrl())
						.addField("Agenda", agenda.toString(), false)
						.addField("Anwesend", String.join("\n", y), true)
						.addField("Vielleicht", String.join("\n", m), true)
						.addField("Abwesend", String.join("\n", n), true)
						.setTimestamp(timestamp)
						.build()
				)
				.setComponents(
						ActionRow.of(
								Button.success("meeting:yes", "Ich kann"),
								Button.secondary("meeting:maybe", "Ich kann vielleicht"),
								Button.danger("meeting:no", "Ich kann nicht"),
								Button.primary("meeting:agenda", "Agenda-Punkt hinzufügen")
						),
						ActionRow.of(StringSelectMenu.create("meeting:agenda_remove")
								.setPlaceholder("Agendapunkt entfernen")
								.addOptions(options)
								.setDisabled(empty)
								.build()
						),
						ActionRow.of(StringSelectMenu.create("meeting:agenda_edit")
								.setPlaceholder("Agendapunkt bearbeiten")
								.addOptions(options)
								.setDisabled(empty)
								.build()
						),
						ActionRow.of(
								Button.danger("meeting:end", "Meeting beenden")
						)
				)
				.setContent(bot.loadGuild(guild).getTeamRole().map(IMentionable::getAsMention).orElse("") + "\nBeginnt " + TimeFormat.RELATIVE.format(nextMeeting))
				.build();
	}

	public interface MeetingHandler {
		void handle(@NotNull List<String> y, @NotNull List<String> m, @NotNull List<String> n, @NotNull List<String> a);
	}
}
