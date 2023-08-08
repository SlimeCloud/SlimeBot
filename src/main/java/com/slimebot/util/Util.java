package com.slimebot.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Util {

	public static String[] parseCamelCase(String s) {
		return Arrays.stream(s.split("(?=[A-Z])"))
				.map(String::toLowerCase)
				.toArray(String[]::new);
	}

	public static boolean isNumeric(String s) {
		if (s==null) return false;
		return Pattern.compile("-?\\d+(\\.\\d+)?").matcher(s).matches();
	}

	public static boolean isValidURL(String url) {
		if (url==null || url.isBlank()) return false;
		try {
			new URL(url).toURI();
			return true;
		} catch (MalformedURLException | URISyntaxException e) {
			return false;
		}
	}

}
