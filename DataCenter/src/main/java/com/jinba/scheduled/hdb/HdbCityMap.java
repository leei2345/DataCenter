package com.jinba.scheduled.hdb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class HdbCityMap {
	
	private static Map<String, String> cityMap = new HashMap<String, String>();

	static {
		cityMap.put("beijing", "1101");
		cityMap.put("shanghai", "3101");
		cityMap.put("guanzhou", "4401");
		cityMap.put("shenzhen", "4403");
		cityMap.put("suzhou", "3205");
		cityMap.put("changsha", "4301");
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
