package com.slimebot.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Util {

	private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("((?<=[a-z])(?=[A-Z]))|((?<=[A-Z])(?=[A-Z][a-z]))");
	private static final Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

	public static String[] parseCamelCase(String s) {
		return Arrays.stream(CAMEL_CASE_PATTERN.split(s))
				.map(String::toLowerCase)
				.toArray(String[]::new);
	}

	public static boolean isNumeric(String s) {
		if (s == null) return false;
		return NUMERIC_PATTERN.matcher(s).matches();
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

}
