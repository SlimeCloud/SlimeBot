package de.slimecloud.slimeball.features.highlights;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
@AllArgsConstructor
public class Highlight implements DataClass<Highlight> {

	private final SlimeBot bot;

	@Column(key = true)
	private final Guild guild;

	@Column(key = true)
	private final String phrase;

	@Column
	private final Set<UserSnowflake> users;

	public Highlight(@NotNull SlimeBot bot) {
		this(bot, null, null, null);
	}

	@NotNull
	@Override
	public Table<Highlight> getTable() {
		return bot.getHighlights();
	}

	@Override
	public String toString() {
		return "Highlight{" +
				"guild=" + guild.getId() +
				", phrase='" + phrase + '\'' +
				", users=" + users.stream().map(UserSnowflake::getId).toList() +
				'}';
	}
}