package de.slimecloud.slimeball.features.poll;

import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Json;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class Poll implements DataClass<Poll> {
	private final SlimeBot bot;

	@Column(key = true)
	private long id;

	@Column
	private int max;

	@Column
	private boolean names;

	@Column
	@Json
	private LinkedHashMap<String, List<String>> values;

	@NotNull
	@Override
	public Table<Poll> getTable() {
		return bot.getPolls();
	}

	@NotNull
	public Poll updateSelection(@NotNull String user, @NotNull List<Integer> selected) {
		AtomicInteger i = new AtomicInteger();

		values.forEach((name, values) -> {
			values.remove(user);
			if (selected.contains(i.getAndIncrement())) values.add(user);
		});

		return this;
	}

	@NotNull
	public String buildChoices(@NotNull Guild guild) {
		StringBuilder result = new StringBuilder();

		AtomicInteger maxLength = new AtomicInteger();
		List<String> participants = new ArrayList<>();

		long count = values.entrySet().stream()
				.peek(e -> maxLength.updateAndGet(c -> Math.max(c, e.getKey().length())))
				.flatMap(e -> e.getValue().stream())
				.peek(participants::add)
				.count();

		values.forEach((name, values) -> {
			double percentage = count == 0 ? 0 : values.size() / (double) count;

			result.append("\033[1m").append(StringUtil.padRight(name, maxLength.get())).append("\033[0m").append(maxLength.get() > 8 ? "\n" : ": ")
					//Bar
					.append("[").append(StringUtil.createProgressBar(percentage, 20)).append("]")
					//Display percentage
					.append(" \033[34;1m(").append((int) (percentage * 100)).append("%, ").append(values.size()).append(" Stimmen)\033[0m")
					.append("\n");

			if (names) {
				StringBuilder names = new StringBuilder();
				values.forEach(n -> names.append("- ").append(Optional.ofNullable(guild.getMemberById(n)).map(Member::getEffectiveName).orElse("Unbekannt")).append("\n"));

				result.append(StringUtils.abbreviate(names.toString(), 500)).append("\n");
			}

			if (maxLength.get() > 8) result.append("\n");
		});

		return "```ansi\n" + StringUtils.abbreviate(result.toString(), 4000) + "```\n" + new HashSet<>(participants).size() + " Teilnehmer" + (max > 1 ? ", " + participants.size() + " Stimmen, Maximal **" + max + "** Stimmen pro Nutzer" : "");
	}
}
