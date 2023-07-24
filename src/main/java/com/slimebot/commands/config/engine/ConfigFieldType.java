package com.slimebot.commands.config.engine;

import de.mineking.discord.commands.inherited.Option;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.function.Function;

public enum ConfigFieldType {
	CHANNEL(field -> new Option(OptionType.CHANNEL, "kanal", field.description()).channelTypes(ChannelType.TEXT, ChannelType.NEWS)),
	ROLE(field -> new Option(OptionType.ROLE, "rolle", field.description())),
	STRING(field -> new Option(OptionType.STRING, "wert",  field.description()));

	public final Function<ConfigField, Option> builder;

	ConfigFieldType(Function<ConfigField, Option> builder) {
		this.builder = builder;
	}
}
