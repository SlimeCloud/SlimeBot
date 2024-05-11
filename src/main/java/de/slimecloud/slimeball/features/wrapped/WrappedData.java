package de.slimecloud.slimeball.features.wrapped;

import de.mineking.databaseutils.Column;
import de.mineking.databaseutils.DataClass;
import de.mineking.databaseutils.Json;
import de.mineking.databaseutils.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class WrappedData implements DataClass<WrappedData> {
	private final SlimeBot bot;

	@Column(key = true)
	private final Guild guild;
	@Column(key = true)
	private final UserSnowflake user;

	@Column
	@Json
	//Using String, Double here instead of Long, Integer because GSON refuses to deserialize it otherwise
	private Map<String, Double> messages = new HashMap<>();
	@Column
	@Json
	private Map<String, Double> emotes = new HashMap<>();

	@Column
	private List<Integer> wordCount = new ArrayList<>();

	@Column
	private int media = 0;
	@Column
	private int links = 0;

	@Column
	@Json
	private Map<String, Double> voice = new HashMap<>();
	@Column
	private int tempVoice = 0;

	@Column
	private int fdmdsSubmitted = 0;
	@Column
	private int fdmdsAccepted = 0;
	@Column
	private Set<Long> fdmdsParticipant = new HashSet<>();

	@Column
	@Json
	private Map<String, Double> xpPerDay = new HashMap<>();

	@Column
	private int messageXp;
	@Column
	private int voiceXp;
	@Column
	private int specialXp;

	public WrappedData(@NotNull SlimeBot bot) {
		this(bot, null, null);
	}

	@NotNull
	public static WrappedData empty(@NotNull SlimeBot bot, @NotNull Guild guild, @NotNull UserSnowflake user) {
		return new WrappedData(bot, guild, user);
	}

	@NotNull
	@Override
	public Table<WrappedData> getTable() {
		return bot.getWrappedData();
	}
}
