package de.slimecloud.slimeball.features.report;

import de.mineking.discordutils.list.ListContext;
import de.mineking.discordutils.list.ListEntry;
import de.mineking.javautils.database.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportBlock implements ListEntry {
	@Column
	private UserSnowflake user;

	@Column
	private long guild;

	@Column
	private String reason;

	@NotNull
	@Override
	public String build(int index, @NotNull ListContext context) {
		return (index + 1) + ". " + user.getAsMention() + ": " + reason;
	}
}
