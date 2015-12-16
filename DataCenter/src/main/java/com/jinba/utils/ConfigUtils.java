package com.jinba.utils;

import java.util.ResourceBundle;

public class ConfigUtils {
	
	private static ResourceBundle config = ResourceBundle.getBundle("config");
	
	public static String getValue (String key) {
		String value = config.getString(key);
		return value;
	}

}
