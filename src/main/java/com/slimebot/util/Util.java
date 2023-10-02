package com.slimebot.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class Util {
	private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("((?<=[a-z])(?=[A-Z]))|((?<=[A-Z])(?=[A-Z][a-z]))");

	public static String[] parseCamelCase(String s) {
		return Arrays.stream(CAMEL_CASE_PATTERN.split(s))
				.map(String::toLowerCase)
				.toArray(String[]::new);
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}

	public static boolean isNumeric(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}

	public static boolean isValidURL(String url) {
		if (url == null || url.isBlank()) return false;
		try {
			new URL(url).toURI();
			return true;
		} catch (MalformedURLException | URISyntaxException e) {
			return false;
		}
	}

	public static String padRight(String s, int n) {
		return padRight(s, ' ', n);
	}

	public static String padLeft(String s, int n) {
		return padLeft(s, ' ', n);
	}

	public static String padRight(String s, char padChar, int n) {
		StringBuilder sb = new StringBuilder(s);
		while (sb.length()<n) sb.append(padChar);
		return sb.toString();
	}

	public static String padLeft(String s, char padChar, int n) {
		StringBuilder sb = new StringBuilder(s);
		while (sb.length()<n) sb.insert(0, padChar);
		return sb.toString();
	}

	public static StringBuilder padRight(StringBuilder sb, char padChar, int n) {
		return new StringBuilder(padRight(sb.toString(), padChar, n));
	}

	public static StringBuilder padLeft(StringBuilder sb, char padChar, int n) {
		return new StringBuilder(padLeft(sb.toString(), padChar, n));
	}

	public static StringBuilder padRight(StringBuilder sb, int n) {
		return new StringBuilder(padRight(sb.toString(), n));
	}

	public static StringBuilder padLeft(StringBuilder sb, int n) {
		return new StringBuilder(padLeft(sb.toString(), n));
	}

	@SuppressWarnings({"unchecked"})
	public static <T> List<T>[] createListArray(int size) {
		List<T>[] result = new List[size];
		Arrays.fill(result, new ArrayList<T>());
		return result;
	}
}
