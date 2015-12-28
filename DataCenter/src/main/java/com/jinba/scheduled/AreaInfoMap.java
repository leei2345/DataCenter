package com.jinba.scheduled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.AreaType;

public class AreaInfoMap {
	
	private static Map<String, Map<AreaType, Map<String, String>>> areaMap = new HashMap<String, Map<AreaType, Map<String, String>>>();
	
	public static String getAreaCode (String areaName, String cityCode) {
		Map<AreaType, Map<String, String>> areaStempMap = areaMap.get(cityCode);
		if (areaStempMap == null) {
			synchronized (AreaInfoMap.class) {
				if (areaStempMap == null) {
					areaStempMap = MysqlDao.getInstance().getAreaMap(cityCode);
					areaMap.put(cityCode, areaStempMap);
				}
			}
		}
		String cityInfo = areaStempMap.get(AreaType.District).get(areaName);
		if (StringUtils.isBlank(cityInfo)) {
			cityInfo = areaStempMap.get(AreaType.DistrictCounty).get(areaName);
		}
		if (StringUtils.isBlank(cityInfo)) {
			cityInfo = areaStempMap.get(AreaType.Nomal).get(areaName);
		}
		return cityInfo;
	}
		
	
	public static void main(String[] args) {
		List<String> l = new ArrayList<String>();
		l.add("s");
		l.add("d");
		l.add("f");
		l.add("g");
		l.add("t");
		System.out.println(Arrays.toString(l.toArray()));
	}
	
}
