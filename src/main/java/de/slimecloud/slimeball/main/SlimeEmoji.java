package de.slimecloud.slimeball.main;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

public enum SlimeEmoji {
	WAVE("slimewave"),
	BONK("bonk"),

	UP("slimesymvup"),
	DOWN("slimesymvdw"),

	SUS("slimeSUS"),

	NUMBER_0("slime0"),
	NUMBER_1("slime1"),
	NUMBER_2("slime2"),
	NUMBER_3("slime3"),
	NUMBER_4("slime4"),
	NUMBER_5("slime5"),
	NUMBER_6("slime6"),
	NUMBER_7("slime7"),
	NUMBER_8("slime8"),
	NUMBER_9("slime9");

	private final String name;

	SlimeEmoji(String name) {
		this.name = name;
	}

	@NotNull
	public Emoji getEmoji(@NotNull Guild guild) {
		return guild.getEmojisByName(name, true).stream()
				.findFirst()
				.map(e -> (Emoji) e) //Cast down to emoji because java is stupid
				.orElse(Emoji.fromUnicode("❌"));
	}

	@NotNull
	public String toString(@NotNull Guild guild) {
		return getEmoji(guild).getFormatted();
	}

	@NotNull
	public static SlimeEmoji number(int i) {
		return valueOf("NUMBER_" + i);
	}
}
