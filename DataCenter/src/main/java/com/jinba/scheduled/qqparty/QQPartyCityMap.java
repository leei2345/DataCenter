package com.jinba.scheduled.qqparty;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class QQPartyCityMap {
	
	private static Map<String, String> cityMap = new HashMap<String, String>();

	static {
		cityMap.put("1_11", "1101");
	}
	
	public static Set<String> getCityList () {
		return cityMap.keySet();
	}
	
	public static String getAreaCode (String cityName) {
		return cityMap.get(cityName);
	}
	
	public static void main(String[] args) {
	}
	
}
