package com.slimebot.commands.config.engine;

import de.mineking.discord.commands.inherited.Option;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum ConfigFieldType {
	CHANNEL("\uD83D\uDCDD", field -> new Option(OptionType.CHANNEL, "kanal", field.description()).channelTypes(ChannelType.TEXT, ChannelType.NEWS), OptionMapping::getAsLong, id -> "<#" + id + ">"),
	VOICE_CHANNEL("\uD83D\uDCDD", field -> new Option(OptionType.CHANNEL, "sprachkanal", field.description()).channelTypes(ChannelType.VOICE, ChannelType.STAGE), OptionMapping::getAsLong, id -> "<#" + id + ">"),
	CHANNEL_LIST("\uD83D\uDCDD", field -> new Option(OptionType.CHANNEL, "kanal", field.description()).channelTypes(ChannelType.TEXT, ChannelType.NEWS).required(), OptionMapping::getAsLong, id -> "<#" + id + ">"),
	ROLE("\uD83E\uDDFB", field -> new Option(OptionType.ROLE, "rolle", field.description()), OptionMapping::getAsLong, id -> "<@&" + id + ">"),
	ROLE_LIST("\uD83E\uDDFB", field -> new Option(OptionType.ROLE, "rolle", field.description()).required(), OptionMapping::getAsLong, id -> "<@&" + id + ">"),
	STRING("", field -> new Option(OptionType.STRING, "wert", field.description()), OptionMapping::getAsString, Object::toString),
	STRING_LIST("", field -> new Option(OptionType.STRING, "wert", field.description()).required(), OptionMapping::getAsString, Object::toString),
	NUMBER("", field -> new Option(OptionType.NUMBER, "wert", field.description()), OptionMapping::getAsDouble, Object::toString),
	NUMBER_LIST("", field -> new Option(OptionType.NUMBER, "wert", field.description()).required(), OptionMapping::getAsDouble, Object::toString);

	private final String emoji;
	private final Function<ConfigField, Option> builder;
	private final Function<OptionMapping, Object> data;
	private final Function<Object, String> formatter;
}
