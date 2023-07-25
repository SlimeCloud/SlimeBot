package com.slimebot.main;

import net.dv8tion.jda.api.entities.IMentionable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Formatter {
	public final static Pattern pattern = Pattern.compile("(?<!\\\\)\\{(?<content>.*?)}");

	public static String format(String format, Map<String, Object> args) {
		Matcher matcher = pattern.matcher(format);

		StringBuilder result = new StringBuilder();

		while (matcher.find()) {
			String token = matcher.group("content");
			Object parsed = parse(token, args);

			matcher.appendReplacement(result, Matcher.quoteReplacement(toString(parsed)));
		}

		matcher.appendTail(result);

		return result.toString();
	}

	public static Object parse(String token, Map<String, Object> args) {
		String[] parts = findParts(token);

		Object current = args.get(parts[0]);

		if(current == null) throw new IllegalStateException("Current object 'null' for token '" + token + "' and parameters " + args);

		try {
			for (int i = 1; i < parts.length; i++) {
				if (!parts[i].contains("(")) {
					current = current.getClass().getField(parts[i]).get(current);
				}

				else {
					String[] method = parts[i].split("\\(", 2);
					String paramString = method[1].substring(0, method[1].length() - 1);

					Object[] methodParameters = Stream.of(paramString.split(", "))
							.filter(param -> !param.isEmpty())
							.map(param -> parse(param, args))
							.toArray(Object[]::new);

					for(Method m : current.getClass().getMethods()) {
						if(!m.getName().equals(method[0])) continue;

						try {
							current = m.invoke(current, methodParameters);
						} catch (IllegalArgumentException ignore) {}
					}
				}
			}
		} catch(NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return current;
	}

	public static String[] findParts(String str) {
		List<String> parts = new ArrayList<>();
		int braceCount = 0;

		StringBuilder temp = new StringBuilder();

		for(char c : str.toCharArray()) {
			if(c == '(') {
				braceCount++;
			}

			if(c == ')') {
				braceCount--;
			}

			if(c == '.' && braceCount == 0) {
				parts.add(temp.toString());
				temp = new StringBuilder();
			}

			else {
				temp.append(c);
			}
		}

		parts.add(temp.toString());

		return parts.toArray(String[]::new);
	}

	public static String toString(Object object) {
		if(object instanceof IMentionable m) {
			return m.getAsMention();
		}

		return object.toString();
	}
}
