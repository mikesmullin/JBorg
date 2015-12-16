package com.sdd.jborg.util;

public final class ValidationHelper
{
	public static String orEquals(String current, String alternative) {
		if (isNotBlank(current)) {
			return current;
		}
		else {
			return alternative;
		}
	}

	public static Integer orEquals(Integer current, Integer alternative) {
		if (current != null) {
			return current;
		}
		else {
			return alternative;
		}
	}

	public static Integer parseInt(String current) {
		try {
			return Integer.parseInt(current, 10);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Boolean isNotBlank(String current) {
		return !( current == null || current.trim().isEmpty() );
	}
}
