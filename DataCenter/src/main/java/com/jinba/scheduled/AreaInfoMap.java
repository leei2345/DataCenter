package com.jinba.scheduled;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.AreaType;

public class AreaInfoMap {
	
	private static Map<AreaType, Map<String, String>> areaMap = new HashMap<AreaType, Map<String, String>>();
	
	public static String getAreaCode (String cityName) {
		if (areaMap.size() == 0) {
			areaMap = MysqlDao.getInstance().getAreaMap();
		}
		String cityInfo = areaMap.get(AreaType.District).get(cityName);
		if (StringUtils.isBlank(cityInfo)) {
			cityInfo = areaMap.get(AreaType.DistrictCounty).get(cityName);
		}
		if (StringUtils.isBlank(cityInfo)) {
			cityInfo = areaMap.get(AreaType.Nomal).get(cityName);
		}
		return cityInfo;
	}
	
}
