package com.slimebot.main;

import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.stream.Stream;

public enum SlimeEmoji {
	SLIME0(0, "<:slime0:1080225248076632064>"),
	SLIME1(1, "<:slime1:1080225250400280788>"),
	SLIME2(2, "<:slime2:1080225252405170216>"),
	SLIME3(3, "<:slime3:1080225254623940669>"),
	SLIME4(4, "<:slime4:1080225256066781215>"),
	SLIME5(5, "<:slime5:1080225257404764180>"),
	SLIME6(6, "<:slime6:1080225259552260127>"),
	SLIME7(7, "<:slime7:1080225261200617533>"),
	SLIME8(8, "<:slime8:1080225263809474610>"),
	SLIME9(9, "<:slime9:1080225265420075168>");

	public final int id;
	public final Emoji emoji;

	SlimeEmoji(int id, String emoji) {
		this.id = id;
		this.emoji = Emoji.fromFormatted(emoji);
	}

	public String format() {
		return emoji.getFormatted();
	}

	public static SlimeEmoji fromId(int id) {
		return Stream.of(values())
				.filter(e -> e.id == id)
				.findAny().orElse(null);
	}

	public static SlimeEmoji fromEmoji(Emoji emoji) {
		return Stream.of(values())
				.filter(e -> e.emoji.equals(emoji))
				.findAny().orElse(null);
	}
}
