package com.slimebot.report.list;

import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.list.ListContext;
import de.mineking.discord.list.ListEntry;
import de.mineking.discord.list.Listable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.time.Instant;
import java.util.List;

public class ReportBlockSet implements Listable<ReportBlockSet.ReportBlock> {
	private final Guild guild;

	public ReportBlockSet(Guild guild) {
		this.guild = guild;
	}

	public static class ReportBlock implements ListEntry {
		public final long id;

		public ReportBlock(long id) {
			this.id = id;
		}

		@Override
		public String build(int index, ListContext<?> context) {
			return (index + 1) + ". " + UserSnowflake.fromId(id).getAsMention();
		}
	}

	@Override
	public List<ReportBlock> getEntries() {
		return Main.database.handle(handle -> handle.createQuery("select \"user\" from report_blocks where guild = :guild")
				.bind("guild", guild.getIdLong())
				.mapTo(long.class)
				.stream()
				.map(ReportBlock::new)
				.toList()
		);
	}

	@Override
	public EmbedBuilder createEmbed(ListContext<ReportBlock> context) {
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(GuildConfig.getColor(guild))
				.setTimestamp(Instant.now())
				.setTitle("Vom Report-System ausgeschlossene Nutzer");

		if (context.entries.isEmpty()) {
			builder.setDescription("*Keine Einträge*");
		}

		else {
			builder.setFooter("Insgesamt " + context.entries.size() + " blockierte Nutzer");
		}

		return builder;
	}
}
