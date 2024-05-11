package de.slimecloud.slimeball.features.level.card;

import de.mineking.javautils.ID;
import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildCardProfile implements DataClass<GuildCardProfile> {
	private final SlimeBot bot;

	@Column(key = true)
	private final Guild guild;

	@Column(key = true)
	private final UserSnowflake user;

	@Setter
	@Column
	private ID id;

	public GuildCardProfile(@NotNull SlimeBot bot, @Nullable Member member) {
		this.bot = bot;

		if (member == null) {
			this.guild = null;
			this.user = null;
		} else {
			guild = member.getGuild();
			user = member;
		}

		this.id = null;
	}

	public GuildCardProfile(@NotNull SlimeBot bot) {
		this(bot, null);
	}

	@NotNull
	public GuildCardProfile empty(@NotNull SlimeBot bot, @NotNull Member member) {
		return new GuildCardProfile(bot, member);
	}

	@NotNull
	@Override
	public Table<GuildCardProfile> getTable() {
		return bot.getCardProfiles();
	}

	@NotNull
	public CardProfileData getData() {
		return bot.getProfileData().getData(id, user).orElseThrow();
	}
}
