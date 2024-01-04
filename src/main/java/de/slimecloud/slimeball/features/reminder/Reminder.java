package de.slimecloud.slimeball.features.reminder;

import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class Reminder implements DataClass<Reminder> {
	private final SlimeBot bot;

	@Column(autoincrement = true, key = true)
	private final int id;
	@Column()
	private final Guild guild;
	@Column()
	private final UserSnowflake user;

	@Column()
	private final long time;

	@Column
	private final String message;

	public Reminder(@NotNull SlimeBot bot) {
		this(bot, 0, null, null, 0, null);
	}

	public void execute() {
		bot.getJda().openPrivateChannelById(user.getIdLong())
				.flatMap(channel -> channel.sendMessage(message))
				.queue();
		delete();
		bot.getRemindManager().scheduleNextReminder();
	}

	@NotNull
	@Override
	public Table<Reminder> getTable() {
		return bot.getReminder();
	}

	@NotNull
	@Override
	public DataClass<Reminder> update() {
		return DataClass.super.update();
	}

	@NotNull
	@Override
	public DataClass<Reminder> delete() {
		return DataClass.super.delete();
	}
}