package com.jinba.scheduled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jinba.dao.MysqlDao;
import com.jinba.scheduled.sogou.SogouListClawer;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;
import com.jinba.utils.LoggerUtil;

@Component
public class SogouTask implements Runnable {

	@Autowired
	private MysqlDao dao;
	@Value("${sgclaw.thread.pool}")
	private int threadPoolSize = 30;
	private ExecutorService listThreadPool;
	
	public void run() {
		List<String> cityList = dao.getAreaList();
		int listSize = cityList.size();
		CountDownLatchUtils listCdl = new CountDownLatchUtils(listSize);
		LoggerUtil.TaskInfoLog("[" + this.getClass().getSimpleName() + "][Start][CitySize " + cityList.size() + "]");
		listThreadPool = Executors.newFixedThreadPool(threadPoolSize);
		for (String eachCity : cityList) {
			String[] cityInfo = eachCity.split("_");
			String cityName = cityInfo[0];
			String areaCode = cityInfo[1];
			Map<Params, String> paramsMap = new HashMap<Params, String>();
			paramsMap.put(Params.area, cityName);
			paramsMap.put(Params.citycode, areaCode);
			SogouListClawer listClawer = new SogouListClawer(paramsMap, listCdl);
			listThreadPool.submit(listClawer);
		}
		try {
			listCdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LoggerUtil.TaskInfoLog("[" + this.getClass().getSimpleName() + "][Done]");
		listThreadPool.shutdownNow();
		listThreadPool = null;
	}

}
