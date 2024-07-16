package de.slimecloud.slimeball.features.staff.absence;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class Absence implements DataClass<Absence>, ListEntry {
	private final SlimeBot bot;

	@Column(key = true)
	private UserSnowflake member;
	@Column(key = true)
	private Guild guild;

	@Column
	private String reason;
	@Column
	private boolean started;

	@Column
	private Date start;
	@Column
	private Date end;

	@NotNull
	@Override
	public Table<Absence> getTable() {
		return bot.getAbsences();
	}

	@NotNull
	@Override
	public String build(int index, @NotNull ListContext<? extends ListEntry> context) {
		return (index + 1) + ". " + member.getAsMention() + ": von " + TimeFormat.DATE_SHORT.format(toInstant(start)) + " bis " + (end == null ? "*`Unbekannt`*" : TimeFormat.RELATIVE.format(toInstant(end))) + "\n " + reason;
	}

	public void start() {
		if (started) return;

		bot.loadGuild(guild).getAbsence().ifPresent(config -> {
			config.getRole().ifPresent(role -> guild.addRoleToMember(member, role).queue());
			config.getChannel().ifPresent(channel -> channel.sendMessageEmbeds(new EmbedBuilder()
					.setAuthor(guild.getMember(member).getEffectiveName(), null, guild.getMember(member).getEffectiveAvatarUrl())
					.setTitle("Abwesenheit Beginnt")
					.setColor(bot.getColor(guild))
					.appendDescription(member.getAsMention() + " ist voraussichtlich bis " + (end == null ? "*`Unbekannt`*" : TimeFormat.RELATIVE.format(toInstant(end))) + " abwesend.\n")
					.appendDescription(member.getAsMention() + " kann ab sofort keine Aufgaben im Team mehr übernehmen.")
					.addField("Grund für die Abwesenheit", reason, false)
					.build()
			).queue());
		});

		bot.loadGuild(guild).getMeeting().ifPresent(config -> {
			if (start.after(new Date(config.getNextMeeting()))) return;
			if (end.before(new Date(config.getNextMeeting()))) return;

			config.updateMessage(guild, (y, m, n, x) -> {
				String mention = member.getAsMention();
				y.remove(mention);
				m.remove(mention);
				n.add(mention);
			});
		});

		started = true;
		update();

		bot.loadGuild(guild).getTeamMessage().ifPresent(config -> config.update(guild));
	}

	@Override
	public boolean delete() {
		boolean value = DataClass.super.delete();

		if (started) {
			bot.loadGuild(guild).getAbsence().ifPresent(config -> {
				config.getRole().ifPresent(role -> guild.removeRoleFromMember(member, role).queue());
				config.getChannel().ifPresent(channel -> channel.sendMessageEmbeds(new EmbedBuilder()
						.setAuthor(guild.getMember(member).getEffectiveName(), null, guild.getMember(member).getEffectiveAvatarUrl())
						.setTitle("Abwesenheit endet")
						.setColor(bot.getColor(guild))
						.appendDescription("Die Abwesenheit von " + member.getAsMention() + " von " + TimeFormat.RELATIVE.format(toInstant(start)) + " hat geendet.\n")
						.appendDescription(member.getAsMention() + " kann ab sofort wieder Aufgaben im Team übernehmen.")
						.addField("Grund für die Abwesenheit", reason, false)
						.build()
				).queue());
			});
			bot.loadGuild(guild).getTeamMessage().ifPresent(config -> config.update(guild));
		}

		return value;
	}

	@NotNull
	public static Instant toInstant(@NotNull Date date) {
		return Instant.ofEpochMilli(date.getTime()).plus(Duration.ofHours(12));
	}
}
