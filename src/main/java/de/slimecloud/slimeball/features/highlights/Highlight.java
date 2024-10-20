package de.slimecloud.slimeball.features.highlights;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Highlight implements DataClass<Highlight> {

	private final SlimeBot bot;

	@ToString.Include
	@Column(key = true)
	private final Guild guild;

	@ToString.Include
	@Column(key = true)
	private final String phrase;

	@Column
	@ToString.Include
	private final Set<UserSnowflake> users;

	public Highlight(@NotNull SlimeBot bot) {
		this(bot, null, null, null);
	}

	@NotNull
	@Override
	public Table<Highlight> getTable() {
		return bot.getHighlights();
	}
}