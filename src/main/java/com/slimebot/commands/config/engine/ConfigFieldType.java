package com.slimebot.commands.config.engine;

import de.mineking.discord.commands.inherited.Option;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.function.Function;

public enum ConfigFieldType {
	CHANNEL("\uD83D\uDCDD", field -> new Option(OptionType.CHANNEL, "kanal", field.description()).channelTypes(ChannelType.TEXT, ChannelType.NEWS), OptionMapping::getAsLong, id -> "<#" + id + ">"),
	ROLE("\uD83E\uDDFB", field -> new Option(OptionType.ROLE, "rolle", field.description()), OptionMapping::getAsLong, id -> "<@&" + id + ">"),
	STRING("", field -> new Option(OptionType.STRING, "wert", field.description()), OptionMapping::getAsString, Object::toString),
	NUMBER("", field -> new Option(OptionType.NUMBER, "wert", field.description()), OptionMapping::getAsDouble, Object::toString);

	public final Function<ConfigField, Option> builder;
	public final Function<OptionMapping, Object> data;
	public final Function<Object, String> formatter;
	public final String emoji;

	ConfigFieldType(String emoji, Function<ConfigField, Option> builder, Function<OptionMapping, Object> data, Function<Object, String> formatter) {
		this.emoji = emoji;
		this.data = data;
		this.builder = builder;
		this.formatter = formatter;
	}
}
