package com.memoryrest.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {
	static Map props = new HashMap();
	static {
		props.put("key1", "fox");
		props.put("key2", "dog");
	}

	public static void main(String[] args) {
		String input = "The quick brown ${key1} jumps over the lazy ${key2}.";

		Pattern p = Pattern.compile("\\$\\{([^}]+)\\}");
		Matcher m = p.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "");
			sb.append(props.get(m.group(1)));
		}
		m.appendTail(sb);
		System.out.println(sb.toString());
	}

	public static String OPEN_KEY = "{";
	public static String DOUBLE_QUOTES = "\"";
	public static String TWO_POINTS = ":";
	public static String COOMA = ",";
	public static String EMPTY = "";
}
