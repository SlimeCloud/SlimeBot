package com.slimebot.report;

import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import de.mineking.discord.list.ListContext;
import de.mineking.discord.list.ListEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class Report implements ListEntry {
	private final long guild;
	private final int id;
	private final Type type;
	private final UserSnowflake issuer;
	private final UserSnowflake target;

	private final Timestamp time;

	private final Status status;
	private final String reason;
	private final String closeReason;

	private Report(long guild, int id, Type type, UserSnowflake issuer, UserSnowflake target, Timestamp timestamp, Status status, String reason, String closeReason) {
		this.guild = guild;
		this.id = id;
		this.type = type;
		this.issuer = issuer;
		this.target = target;
		this.time = timestamp;
		this.status = status;
		this.reason = reason;
		this.closeReason = closeReason;
	}

	public static Report createReport(Guild guild, Type type, User issuer, User target, String reason) {
		int id = Main.database.handle(handle -> handle.createUpdate("insert into reports(guild, issuer, target, type, message) values(:guild, :issuer, :target, :type, :message)")
				.bind("guild", guild.getIdLong())
				.bind("issuer", issuer.getIdLong())
				.bind("target", target.getIdLong())
				.bind("type", type.toString())
				.bind("message", reason)
				.executeAndReturnGeneratedKeys("id")
				.mapTo(int.class).one()
		);

		return get(guild, id).orElseThrow();
	}

	public static Optional<Report> get(Guild guild, int id) {
		return Main.database.handle(handle -> handle.createQuery("select * from reports where guild = :guild and id = :id")
				.bind("guild", guild.getIdLong())
				.bind("id", id)
				.mapTo(Report.class)
				.findOne()
		);
	}

	public void log() {
		MessageChannel logChannel = Main.database.getChannel(Main.jdaInstance.getGuildById(guild), DatabaseField.PUNISHMENT_CHANNEL);

		if(logChannel == null) return;

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTimestamp(Instant.now())
				.setColor(Main.database.getColor(guild))
				.setTitle(":exclamation: Neuer Report!")
				.addField("Report von:", issuer.getAsMention(), true)
				.addField("Gemeldet:", target.getAsMention(), true);

		if(type == Type.MESSAGE) {
			embedBuilder
					.setDescription("Es wurde eine Nachricht gemeldet!")
					.addField("Nachricht:", reason, false);
		}

		else {
			embedBuilder
					.setDescription("Es wurde eine Person gemeldet!")
					.addField("Begründung:", reason, false);
		}

		logChannel.sendMessage(buildMessage()).queue();
	}

	public void close(String reason) {
		Main.database.run(handle -> handle.createUpdate("update reports set status = 'CLOSED', closeReason = :reason where guild = :guild and id = :id")
				.bind("reason", reason)
				.bind("guild", guild)
				.bind("id", id)
				.execute()
		);
	}

	public MessageEmbed buildEmbed() {
		EmbedBuilder embed = new EmbedBuilder()
				.setColor(Main.database.getColor(guild))
				.setTimestamp(Instant.now())
				.setTitle(":exclamation:  Details zu Report #" + id)
				.addField("Report Typ:", type.str, true)
				.addField("Gemeldeter User:", target.getAsMention(), true)
				.addField("Gemeldet von:", issuer.getAsMention(), true)
				.addField("Gemeldet am:", TimeFormat.DEFAULT.format(time.toInstant()), true)
				.addField("Status:", status.str, true);

		embed.addField(type == Type.MESSAGE ? "Gemeldete Nachricht:" : "Meldegrund:", reason, false);

		if(!isOpen()) {
			embed.addField("Verfahren:", closeReason, true);
		}

		return embed.build();
	}

	public MessageCreateData buildMessage() {
		MessageCreateBuilder builder = new MessageCreateBuilder()
				.setEmbeds(buildEmbed());

		if(isOpen()) {
			builder.setActionRow(Button.danger("report:close", "Close #" + id).withEmoji(Emoji.fromUnicode("\uD83D\uDD12")));
		}

		return builder.build();
	}

	public int getId() {
		return id;
	}

	public boolean isOpen() {
		return status == Status.OPEN;
	}

	@Override
	public String build(int index, ListContext<?> context) {
		return (index + 1) + ". [" + status.emoji + "] " + TimeFormat.DEFAULT.format(time.toInstant()) + ": " + target.getAsMention() + " gemeldet von " + issuer.getAsMention();
	}

	public static class ReportRowMapper implements RowMapper<Report> {
		@Override
		public Report map(ResultSet rs, StatementContext ctx) throws SQLException {
			return new Report(
					rs.getLong("guild"),
					rs.getInt("id"),
					Type.valueOf(rs.getString("type")),
					UserSnowflake.fromId(rs.getLong("issuer")),
					UserSnowflake.fromId(rs.getLong("target")),
					rs.getTimestamp("time"),
					Status.valueOf(rs.getString("status")),
					rs.getString("message"),
					rs.getString("closeReason")
			);
		}
	}
}
