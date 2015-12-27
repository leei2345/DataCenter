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
	
	private static Map<AreaType, Map<String, String>> areaMap = new HashMap<AreaType, Map<String, String>>();
	private static Map<AreaType, Map<String, Map<String, String>>> muiltAreaMap = new HashMap<AreaType, Map<String, Map<String, String>>>();
	
	public static String getAreaCode (String areaName, String cityName) {
		if (areaMap.size() == 0) {
			areaMap = MysqlDao.getInstance().getAreaMap();
		}
		if (muiltAreaMap.size() == 0) {
			muiltAreaMap = MysqlDao.getInstance().getMuiltAreaMap();
		}
		Map<String, String> muiltInfo = muiltAreaMap.get(AreaType.FirstStemp).get(areaName);
		String cityInfo = muiltInfo.get(cityName);
		if (StringUtils.isBlank(cityInfo)) {
			cityInfo = areaMap.get(AreaType.District).get(areaName);
		}
		if (StringUtils.isBlank(cityInfo)) {
			cityInfo = areaMap.get(AreaType.DistrictCounty).get(areaName);
		}
		if (StringUtils.isBlank(cityInfo)) {
			cityInfo = areaMap.get(AreaType.Nomal).get(areaName);
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
