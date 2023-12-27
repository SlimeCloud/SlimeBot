package de.slimecloud.slimeball.features.poll;

import de.mineking.javautils.database.Column;
import de.mineking.javautils.database.DataClass;
import de.mineking.javautils.database.Json;
import de.mineking.javautils.database.Table;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@AllArgsConstructor
public class Poll implements DataClass<Poll> {
	private final SlimeBot bot;

	@Column(key = true)
	private long id;

	@Column
	private int max;

	@Column
	@Json
	private LinkedHashMap<String, List<String>> values;

	@NotNull
	@Override
	public Table<Poll> getTable() {
		return bot.getPolls();
	}

	@NotNull
	public Poll updateSelection(String user, @NotNull List<Integer> selected) {
		AtomicInteger i = new AtomicInteger();

		values.forEach((name, values) -> {
			values.remove(user);
			if (selected.contains(i.getAndIncrement())) values.add(user);
		});

		return this;
	}

	public String buildChoices() {
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

			result.append("\033[1m").append(StringUtil.padRight(name, maxLength.get())).append("\033[0m").append(": ")
					//Bar
					.append("[").append(StringUtil.createProgressBar(percentage, 30)).append("]")
					//Display percentage
					.append(" \033[34;1m(").append((int) (percentage * 100)).append("%, ").append(values.size()).append(" Stimmen)\033[0m")
					.append("\n");
		});

		return "```ansi\n" + result + "```\n" + new HashSet<>(participants).size() + " Teilnehmer" + (max > 1 ? ", " + participants.size() + " Stimmen" : "");
	}
}
