package de.slimecloud.slimeball.features.staff.absence;

import de.mineking.databaseutils.exception.ConflictException;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.list.ListManager;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.time.Month;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
@ApplicationCommand(name = "absence", description = "Verwaltet Abwesenheit", scope = Scope.GUILD)
public class AbsenceCommand {
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getAbsence().isPresent();
	public final CommandPermission permission = CommandPermission.TEAM;

	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull Command<ICommandContext> command, @NotNull ListManager<ICommandContext> list) {
		new AbsenceScheduler(bot);

		command.addSubcommand(list.createCommand(state -> bot.getAbsences()).withDescription("Zeigt alle Abwesenheiten an"));
	}

	@ApplicationCommand(name = "set", description = "Setzt deinen Status auf Abwesend")
	public static class SetCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlimeBot bot,
		                           @Option(description = "Der Grund warum du weg bist") String reason,
		                           @Option(description = "Der Tag, an dem du zurück bist", name = "endday", required = false, minValue = 1, maxValue = 31) Integer endDay,
		                           @Option(description = "Der Monat, an dem du zurück bist", name = "endmonth", required = false) Month endMonth,
		                           @Option(description = "Das Jahr, an dem du zurück bist", name = "endyear", required = false, minValue = 2024) Integer endYear
		) {
			Date end = getTimestamp(endDay, endMonth, endYear);

			if (end != null && end.before(new java.util.Date())) {
				event.reply(":x: Ungültiges End-Datum!").setEphemeral(true).queue();
				return;
			}

			try {
				bot.getAbsences().create(event.getMember(), reason, new Date(System.currentTimeMillis()), end).start();

				event.reply(":white_check_mark: Du wurdest als abwesend gemeldet").setEphemeral(true).queue();
			} catch (ConflictException e) {
				event.reply(":x: Für dich ist bereits eine Abwesenheit registriert!").setEphemeral(true).queue();
			}
		}
	}

	@ApplicationCommand(name = "schedule", description = "Setzt deinen Status für Später auf Abwesend")
	public static class ScheduleCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlimeBot bot,
		                           @Option(description = "Der Grund warum du weg bist") String reason,
		                           @Option(description = "Der Tag, ab dem du weg bist", name = "startday", minValue = 1, maxValue = 31) Integer startDay,
		                           @Option(description = "Der Monat, ab dem du weg bist", name = "startmonth", required = false) Month startMonth,
		                           @Option(description = "Das Jahr, ab dem du weg bist", name = "startyear", required = false, minValue = 2024) Integer startYear,
		                           @Option(description = "Der Tag, an dem du zurück bist", name = "endday", required = false, minValue = 1, maxValue = 31) Integer endDay,
		                           @Option(description = "Der Monat, an dem du zurück bist", name = "endmonth", required = false) Month endMonth,
		                           @Option(description = "Das Jahr, an dem du zurück bist", name = "endyear", required = false, minValue = 2024) Integer endYear
		) {
			Date start = getTimestamp(startDay, startMonth, startYear);
			if (start == null) return; //This should not happen

			Date expiry = getTimestamp(endDay, endMonth, endYear);

			if (start.before(new java.util.Date())) {
				event.reply(":x: Ungültiges Start-Datum!").setEphemeral(true).queue();
				return;
			}

			if (expiry != null && expiry.before(start)) {
				event.reply(":x: Ungültiges End-Datum!").setEphemeral(true).queue();
				return;
			}

			try {
				bot.getAbsences().create(event.getMember(), reason, start, expiry);

				event.reply(":white_check_mark: Du wirst " + TimeFormat.RELATIVE.format(Absence.toInstant(start)) + " als abwesend gemeldet").setEphemeral(true).queue();
			} catch (ConflictException e) {
				event.reply(":x: Für dich ist bereits eine Abwesenheit registriert!").setEphemeral(true).queue();
			}
		}
	}

	@ApplicationCommand(name = "unset", description = "Setzt deinen Status auf Anwesend")
	public static class UnsetCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlimeBot bot) {
			bot.getAbsences().getAbsence(event.getMember()).ifPresentOrElse(
					absence -> {
						absence.delete();
						event.reply(":white_check_mark: Deine Abwesenheit wurde entfernt").setEphemeral(true).queue();
					},
					() -> event.reply(":x: Für dich ist keine eine Abwesenheit registriert!").setEphemeral(true).queue()
			);
		}
	}

	@Nullable
	@SuppressWarnings("deprecation")
	public static Date getTimestamp(@Nullable Integer day, @Nullable Month month, @Nullable Integer year) {
		if (day == null) return null;
		return new Date((year == null ? ZonedDateTime.now(Main.timezone).getYear() : year) - 1900, (month == null ? ZonedDateTime.now(Main.timezone).getMonthValue() : month.getValue()) - 1, day);
	}
}
