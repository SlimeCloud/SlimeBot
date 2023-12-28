package de.slimecloud.slimeball.config.engine;

import de.slimecloud.slimeball.util.ColorUtil;
import de.slimecloud.slimeball.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum ConfigFieldType {
	MESSAGE_CHANNEL(OptionType.CHANNEL, o -> o.setChannelTypes(ChannelType.TEXT, ChannelType.NEWS, ChannelType.FORUM), OptionMapping::getAsLong, x -> true, x -> x, id -> "<#" + id + ">"),
	VOICE_CHANNEL(OptionType.CHANNEL, o -> o.setChannelTypes(ChannelType.VOICE, ChannelType.STAGE), OptionMapping::getAsLong, x -> true, x -> x, id -> "<#" + id + ">"),
	ALL_CHANNEL(OptionType.CHANNEL, o -> o, OptionMapping::getAsLong, x -> true, x -> x, id -> "<#" + id + ">"),
	ROLE(OptionType.ROLE, o -> o, OptionMapping::getAsLong, x -> true, x -> x, id -> "<@&" + id + ">"),

	COLOR(OptionType.STRING, (t, o) -> o, (t, o) -> {
		try {
			//Validate
			if (ColorUtil.parseColor(o.getAsString()) != null) return o.getAsString();
			else throw new ValidationException(null);
		} catch (Exception e) {
			throw new ValidationException(e);
		}
	}, (t, s) -> ColorUtil.parseColor(s) != null, (t, c) -> t.isAssignableFrom(String.class) ? c : ColorUtil.parseColor(c), v -> v instanceof Color c ? ColorUtil.toString(c) : v.toString()),
	URL(OptionType.STRING, o -> o, o -> {
		try {
			//Validate
			new URL(o.getAsString());
			return o.getAsString();
		} catch (MalformedURLException e) {
			throw new ValidationException(e);
		}
	}, StringUtil::isValidURL, s -> s, Objects::toString),

	ENUM(OptionType.STRING,
			(t, o) -> {
				for (Object c : t.getEnumConstants()) o.addChoice(c.toString(), ((Enum<?>) c).name());
				return o;
			},
			(t, o) -> Arrays.stream(t.getEnumConstants())
					.filter(c -> ((Enum<?>) c).name().equals(o.getAsString()))
					.findAny().orElseThrow(),
			(t, s) -> Arrays.stream(t.getEnumConstants()).anyMatch(c -> ((Enum<?>) c).name().equals(s)),
			(t, s) -> Arrays.stream(t.getEnumConstants())
					.filter(c -> ((Enum<?>) c).name().equals(s))
					.findAny().orElseThrow(),
			Object::toString
	),
	ENUM_SET(null, null, null, null, (t, o) -> null, null),

	STRING(OptionType.STRING, o -> o, OptionMapping::getAsString, x -> true, x -> x, Objects::toString),
	INTEGER(OptionType.INTEGER, o -> o, OptionMapping::getAsInt, StringUtil::isInteger, Integer::parseInt, Objects::toString),
	NUMBER(OptionType.NUMBER, o -> o, OptionMapping::getAsDouble, StringUtil::isNumeric, Double::parseDouble, Objects::toString);

	private final OptionType type;
	private final BiFunction<Class<?>, OptionData, OptionData> configuration;
	private final BiFunction<Class<?>, OptionMapping, Object> extractor;

	private final BiPredicate<Class<?>, String> check;
	private final BiFunction<Class<?>, String, Object> parse;

	private final Function<Object, String> string;

	ConfigFieldType(OptionType type, Function<OptionData, OptionData> configuration, Function<OptionMapping, Object> extractor, Predicate<String> check, Function<String, Object> parse, Function<Object, String> string) {
		this(type, (t, o) -> configuration.apply(o), (t, o) -> extractor.apply(o), (t, s) -> check.test(s), (t, s) -> parse.apply(s), string);
	}
}
