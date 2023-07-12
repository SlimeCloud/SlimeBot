package com.slimebot.report.assets;

import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class Report {
	private final long guild;
	private final int id;
	private final Type type;
	private final User issuer;
	private final User target;

	private final Timestamp time;

	private final Status status;
	private final String reason;
	private final String closeReason;

	private Report(long guild, int id, Type type, User issuer, User target, Timestamp timestamp, Status status, String reason, String closeReason) {
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
				.bind("guild", guild.getId())
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
		return Main.database.handle(handle -> handle.createQuery("select from reports where guild = :guild and id = :id")
				.bind("guild", guild.getId())
				.bind("id", id)
				.mapTo(Report.class)
				.findOne()
		);
	}

	public void log() {
		MessageChannel logChannel = Main.database.getChannel(Main.jdaInstance.getGuildById(guild), DatabaseField.PUNISHMENT_CHANNEL);

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
		Main.database.run(handle -> handle.createUpdate("update reports set status = CLOSED, closeReason = :reason where guild = :guild and id = :id")
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
				.addField("Gemeldet am:", time.toLocalDateTime().format(Main.dateFormat) + "Uhr", true)
				.addField("Status:", status.str, true);

		if(type == Type.MESSAGE) {
			embed.addField("Gemeldete Nachricht:", reason, false);
		}

		else if(type == Type.USER) {
			embed.addField("Meldegrund:", reason, true);
		}

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

	public String shortDescription() {
		return target.getAsMention() + " wurde am ` " + time.toLocalDateTime().format(Main.dateFormat) + "` von " + issuer.getAsMention() + " gemeldet.";
	}

	public static class ReportRowMapper implements RowMapper<Report> {
		@Override
		public Report map(ResultSet rs, StatementContext ctx) throws SQLException {
			return new Report(
					rs.getLong("guild"),
					rs.getInt("id"),
					Type.valueOf(rs.getString("type")),
					Main.jdaInstance.getUserById(rs.getLong("issuer")),
					Main.jdaInstance.getUserById(rs.getLong("target")),
					rs.getTimestamp("time"),
					Status.valueOf(rs.getString("status")),
					rs.getString("message"),
					rs.getString("closeReason")
			);
		}
	}
}




