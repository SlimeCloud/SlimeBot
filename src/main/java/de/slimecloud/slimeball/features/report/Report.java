package de.slimecloud.slimeball.features.report;

import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class Report implements ListEntry, DataClass<Report> {
	private final SlimeBot bot;

	@Column(key = true, autoincrement = true)
	private int id;

	@Column
	private Type type;

	@Column
	private Guild guild;
	@Column
	private UserSnowflake issuer;
	@Column
	private UserSnowflake target;

	@Column
	private Instant time;

	@Column
	private String message;

	@Column
	private Status status;
	@Column
	private String closeReason;

	public Report(@NotNull SlimeBot bot, @NotNull Type type, @NotNull Guild guild, @NotNull UserSnowflake issuer, @NotNull UserSnowflake target, @NotNull String message) {
		this(bot, 0, type, guild, issuer, target, Instant.now(), message, Status.OPEN, null);
	}

	@NotNull
	@Override
	public Table<Report> getTable() {
		return bot.getReports();
	}

	public boolean isOpen() {
		return status == Status.OPEN;
	}

	public void close(@NotNull String reason) {
		status = Status.CLOSED;
		closeReason = reason;
		update();
	}

	@NotNull
	public MessageEmbed buildEmbed(@NotNull String title) {
		EmbedBuilder embed = new EmbedBuilder()
				.setTitle(status.getEmoji() + " " + title + " #" + id)
				.setColor(bot.getColor(guild))
				.setTimestamp(time)

				//Add users
				.addField("Gemeldeter Nutzer", target.getAsMention(), true)
				.addField("Gemeldet von", issuer.getAsMention(), true)
				.addBlankField(true) //This is to get the other information to the next row

				//Add other information
				.addField("Report Typ", type.getStr(), true)
				.addField("Status", status.getStr(), true);

		//Add close reason if closed
		if (!isOpen()) embed.addField("Verfahren", closeReason, true);

		//Add reason
		embed.addField(type == Type.MESSAGE ? "Gemeldete Nachricht" : "Meldegrund", message, false);

		return embed.build();
	}

	@NotNull
	public MessageCreateData buildMessage(@NotNull String title) {
		MessageCreateBuilder builder = new MessageCreateBuilder().setEmbeds(buildEmbed(title));
		//Add close button in case this report isn't closed already
		if (isOpen())
			builder.setActionRow(Button.danger("report:close:" + id, "Report schließen").withEmoji(Emoji.fromUnicode("\uD83D\uDD12")));
		return builder.build();
	}

	@NotNull
	@Override
	public String build(int index, @NotNull ListContext context) {
		//Escaping the dot prevents discord from making this a numbered list. The problem with these is that the numbering is corrected automatically which might cause the displayed ids to be wrong.
		return id + "\\. [" + status.getEmoji() + "] " + TimeFormat.DEFAULT.format(time) + ": " + target.getAsMention() + " gemeldet von " + issuer.getAsMention();
	}
}
