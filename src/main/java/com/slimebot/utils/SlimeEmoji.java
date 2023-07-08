package com.slimebot.utils;

import net.dv8tion.jda.api.entities.emoji.Emoji;

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


	private final int id;
	private final String string;

	SlimeEmoji(int id, String string) {
		this.id = id;
		this.string = string;
	}

	public int getId() {return this.id;}

	public String getAsString() {
		return this.string;
	}

	public Emoji getEmoji() {
		return Emoji.fromFormatted(this.string);
	}

	public static SlimeEmoji fromId(int id) {
		for(SlimeEmoji emoji : SlimeEmoji.values()) {
			if(emoji.id == id) return emoji;
		}
		return null;
	}

	public static SlimeEmoji fromEmoji(Emoji emoji) {
		String formatted = emoji.getFormatted();
		for(SlimeEmoji slimeEmoji : SlimeEmoji.values()) {
			if(slimeEmoji.string.equals(formatted)) return slimeEmoji;
		}
		return null;
	}
}
