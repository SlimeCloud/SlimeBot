package com.slimebot.commands.report;

import com.slimebot.main.CommandContext;
import com.slimebot.main.CommandPermission;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.report.list.ReportBlockSet;
import de.mineking.discord.commands.CommandManager;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.commands.annotated.option.Option;
import de.mineking.discord.list.ListCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;

@ApplicationCommand(name = "block", description = "Verwaltet vom Report-System ausgeschlossene Nutzer")
public class BlockCommand {
	public static boolean isBlocked(Member member) {
		return Main.database.handle(handle -> handle.createQuery("select count(*) from report_blocks where guild = :guild and \"user\" = :user")
				.bind("guild", member.getGuild().getIdLong())
				.bind("user", member.getIdLong())
				.mapTo(int.class).one()
		) > 0;
	}

	@WhenFinished
	public void setup(CommandManager<CommandContext> manager) {
		manager.registerCommand("report block list", new BlockListCommand());
	}

	@ApplicationCommand(name = "add", description = "Fügt einen Nutzer zur Block-Liste hinzu, sodass er keine Meldungen mehr machen kann")
	public static class AddCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event, @Option(description = "Der Nutzer, der blockiert werden soll") Member user) {
			if(isBlocked(user)) {
				event.replyEmbeds(
						new EmbedBuilder()
								.setTimestamp(Instant.now())
								.setColor(GuildConfig.getColor(event.getGuild()))
								.setTitle(":exclamation: Error: Already blocked!")
								.setDescription(user.getAsMention() + " ist bereits blockiert")
								.build()
				).setEphemeral(true).queue();
				return;
			}

			Main.database.run(handle -> handle.createUpdate("insert into report_blocks values(:guild, :user)")
					.bind("guild", event.getGuild().getIdLong())
					.bind("user", user.getIdLong())
					.execute()
			);

			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(GuildConfig.getColor(event.getGuild()))
							.setTitle(":white_check_mark: Erfolgreich Blockiert")
							.setDescription(user.getAsMention() + " wurde blockiert und kann nun keine Reports mehr erstellen")
							.build()
			).setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "remove", description = "Gibt einen Nutzer wieder für Meldungen frei")
	public static class RemoveCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event, @Option(description = "Der Nutzer, der ent-blockiert werden soll") Member user) {
			if(!isBlocked(user)) {
				event.replyEmbeds(
						new EmbedBuilder()
								.setTimestamp(Instant.now())
								.setColor(GuildConfig.getColor(event.getGuild()))
								.setTitle(":exclamation: Error: Not Found")
								.setDescription(user.getAsMention() + " konnte nicht in der Blockliste gefunden werden!")
								.build()
				).setEphemeral(true).queue();
				return;
			}

			Main.database.run(handle -> handle.createUpdate("delete from report_blocks where guild = :guild and \"user\" = :user")
					.bind("guild", event.getGuild().getIdLong())
					.bind("user", user.getIdLong())
					.execute()
			);

			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(GuildConfig.getColor(event.getGuild()))
							.setTitle(":white_check_mark: Entblockt")
							.setDescription(user.getAsMention() + " kann nun wieder Reports erstellen")
							.build()
			).setEphemeral(true).queue();
		}
	}

	public static class BlockListCommand extends ListCommand<CommandContext, ReportBlockSet.ReportBlock, ReportBlockSet> {
		public BlockListCommand() {
			super(CommandPermission.TEAM,
					(context, options) -> new ReportBlockSet(context.guild)
			);

			description = "Zeigt alle vom Report-System ausgeschlossenen Nutzer";
		}
	}
}
