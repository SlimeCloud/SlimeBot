package de.slimecloud.slimeball.features.staff;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@RequiredArgsConstructor
@ApplicationCommand(name = "absence", description = "Abwesenheit an/abmelden")
public class AbsenceCommand {
	private final SlimeBot bot;
	public final CommandPermission permission = CommandPermission.ROLE_MANAGE;

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "Bist du Abwesend") boolean absent,
                               @Option(description = "Bis wann bist du Abwesend?", required = false) String duration
	) {
		GuildConfig config = bot.loadGuild(event.getGuild());
		config.getAbsenceRole().ifPresentOrElse(role -> {
			if (absent) event.getGuild().addRoleToMember(event.getUser(), role).queue();
			else event.getGuild().removeRoleFromMember(event.getUser(), role).queue();

			event.replyEmbeds(new EmbedBuilder()
					.setTitle(":white_check_mark: Abwesenheit geupdatet")
					.setColor(bot.getColor(event.getGuild()))
					.setDescription("Du bist nun " + (absent ? "Abwesend" + (duration == null ? "" : " bis " + duration) : "wieder Anwesend") + "!")
					.setTimestamp(Instant.now())
					.build()).queue();

			config.getLogChannel().ifPresent(channel -> channel.sendMessageEmbeds(new EmbedBuilder()
					.setTitle("Abwesenheit update!")
					.setColor(bot.getColor(event.getGuild()))
					.setDescription(event.getMember().getAsMention() + " ist nun " + (absent ? "Abwesend!" : "wieder Anwesend!"))
					.setTimestamp(Instant.now())
					.build()).queue());

		}, () -> event.reply("Es ist keine Rolle für Abwesenheit eingestellt!").setEphemeral(true).queue());
	}
}
