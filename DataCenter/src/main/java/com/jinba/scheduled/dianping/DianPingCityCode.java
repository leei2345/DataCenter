package com.jinba.scheduled.dianping;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class DianPingCityCode {
	
	private static Map<Integer, String> codeMap = new HashMap<Integer, String>();

	static {

		codeMap.put(2, "北京市");
		
	}
	
	public static String getCityName (int cityCode) {
		String cityName = codeMap.get(cityCode);
		if (StringUtils.isBlank(cityName)) {
			return "";
		}
		return cityName;
	}

	
	public static void main(String[] args) {}

}
