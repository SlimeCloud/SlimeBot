package de.slimecloud.slimeball.features.level.card;

import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildCardProfile implements DataClass<GuildCardProfile> {
	private final SlimeBot bot;

	@Column(key = true)
	private final long guild;

	@Column
	private final UserSnowflake user;

	@Setter
	@Accessors(chain = true)
	@Column
	private int id;

	public GuildCardProfile(@NotNull SlimeBot bot, @Nullable Member member) {
		this.bot = bot;

		if(member == null) {
			this.guild = 0;
			this.user = null;
		} else {
			guild = member.getGuild().getIdLong();
			user = member;
		}

		this.id = 0;
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
