package de.slimecloud.slimeball.features.staff.absence;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.*;

@RequiredArgsConstructor
@ApplicationCommand(name = "absence", description = "Abwesenheit an/abmelden")
public class AbsenceCommand {
	private final SlimeBot bot;
	public final CommandPermission permission = CommandPermission.ROLE_MANAGE;

	@Setup
	public static void setup(@NotNull SlimeBot bot) {
		bot.scheduleDaily(12, new Absence(bot).check());
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "Bist du Abwesend") boolean absent,
	                           @Option(description = "Der Grund warum du weg bist", required = false) String reason,
	                           @Option(description = "Der Tag, an dem du zurück bist", required = false, minValue = 1, maxValue = 31) Integer day,
	                           @Option(description = "Der Monat, an dem du zurück bist", required = false) Month month,
	                           @Option(description = "Das Jahr, an dem du zurück bist", required = false, minValue = 2024) Integer year

	) {
		GuildConfig config = bot.loadGuild(event.getGuild());

		if (absent) {
			try {
				Instant timestamp;
				if (day != null && month != null) {
					ZonedDateTime now = LocalDateTime.now().atZone(Main.timezone);
					timestamp = LocalDateTime.of((year == null ? now.getYear() : year), month, day, 12, 0, 0).atZone(Main.timezone).toInstant();
				} else timestamp = null;

				config.getAbsenceRole().ifPresentOrElse(role -> {
					event.getGuild().addRoleToMember(event.getUser(), role).queue();

					bot.getAbsences().getByUser(event.getUser()).ifPresentOrElse(absence ->
									event.replyEmbeds(new EmbedBuilder()
											.setTitle(":x: Fehler")
											.setColor(bot.getColor(event.getGuild()))
											.setDescription("Du bist bereits Abwesend!")
											.setTimestamp(Instant.now())
											.build()).setEphemeral(true).queue(),
							() -> {
								bot.getAbsences().addAbsence(new Absence(bot, event.getUser(), event.getGuild(), timestamp));

								event.replyEmbeds(new EmbedBuilder()
										.setTitle(":white_check_mark: Abwesenheit geupdatet")
										.setColor(bot.getColor(event.getGuild()))
										.setDescription("Du bist nun Abwesend" + (timestamp == null ? "" : " bis " + TimeFormat.DATE_SHORT.format(timestamp)) + "!" + (reason == null ? "" : "\n\n**Grund:**\n" + reason))
										.setTimestamp(Instant.now())
										.build()).queue();
							});
				}, () -> event.reply("Es ist keine Rolle für Abwesenheit eingestellt!").setEphemeral(true).queue());
			} catch (DateTimeException e) {
				event.reply(":x: " + e.getMessage()).setEphemeral(true).queue();
			}
		} else {
			config.getAbsenceRole().ifPresentOrElse(role -> {
						event.getGuild().removeRoleFromMember(event.getUser(), role).queue();

						bot.getAbsences().getByUser(event.getUser()).ifPresentOrElse(absence -> {
									bot.getAbsences().remove(absence);
									event.replyEmbeds(new EmbedBuilder()
											.setTitle(":white_check_mark: Abwesenheit geupdatet")
											.setColor(bot.getColor(event.getGuild()))
											.setDescription("Du bist nun wieder Anwesend!")
											.setTimestamp(Instant.now())
											.build()).queue();
								},
								() -> event.replyEmbeds(new EmbedBuilder()
										.setTitle(":x: Fehler")
										.setColor(bot.getColor(event.getGuild()))
										.setDescription("Du bist bereits Anwesend!")
										.setTimestamp(Instant.now())
										.build()).setEphemeral(true).queue()
						);
					},
					() -> event.reply("Es ist keine Rolle für Abwesenheit eingestellt!").setEphemeral(true).queue());
		}
	}
}
