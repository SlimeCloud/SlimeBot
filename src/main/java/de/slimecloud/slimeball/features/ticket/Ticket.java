package de.slimecloud.slimeball.features.ticket;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.mineking.databaseutils.exception.ConflictException;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class Ticket implements DataClass<Ticket> {

	private final transient SlimeBot bot;

	@Column(key = true)
	private final Guild guild;
	@Column(key = true)
	private final UserSnowflake user;
	@Column(key = true)
	private final long id;


	@Column
	private final UserSnowflake creator;
	@Column
	private final UserSnowflake closer;
	@Column
	private final UserSnowflake claimer;
	@Column
	private final long openTime;
	@Column
	private final long closeTime;
	@Column
	private final String closeReason;

	public Ticket(SlimeBot bot) {
		this(bot, null, null, 0, null, null, null, 0, 0, "");
	}


	/**
	 * true if staff opened a ticket with this user
	 */
	public boolean isStaffTicket() {
		return user.getIdLong() != creator.getIdLong();
	}
	@NotNull
	@Override
	public Table<Ticket> getTable() {
		return bot.getTicket();
	}

	@NotNull
	@Override
	public Ticket update() throws ConflictException {
		return DataClass.super.update();
	}
}
