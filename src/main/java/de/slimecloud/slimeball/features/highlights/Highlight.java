package de.slimecloud.slimeball.features.highlights;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
@ToString
@AllArgsConstructor
public class Highlight implements DataClass<Highlight>, ListEntry {

	@ToString.Exclude
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

	@NotNull
	@Override
	public String build(int index, @NotNull ListContext<? extends ListEntry> context) {
		return (index + 1) + ". `" + phrase + "`";
	}
}