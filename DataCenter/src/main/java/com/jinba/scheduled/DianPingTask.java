package com.jinba.scheduled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jinba.core.BaseDetailClawer;
import com.jinba.core.BaseListClawer;
import com.jinba.dao.MysqlDao;
import com.jinba.pojo.AnalysisType;
import com.jinba.pojo.XiaoQuEntity;
import com.jinba.scheduled.dianping.DianPingDetailClawer;
import com.jinba.scheduled.dianping.DianPingListClawer;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;
import com.jinba.utils.LoggerUtil;

@Component
public class DianPingTask implements Runnable {

	private String tempUrl;
	private int xiaoquType;
	private AnalysisType analysisType;
	@Resource
	private MysqlDao dao;
	@Value("${dpclaw.thread.pool}")
	private int threadPoolSize = 40;
	private static ExecutorService detailThreadPool;
	private static ExecutorService listThreadPool;
	
	public DianPingTask (String tempUrl, int xiaoquType, AnalysisType analysisType) {
		this.tempUrl = tempUrl;
		this.xiaoquType = xiaoquType;
		this.analysisType = analysisType;
	}
	
	public DianPingTask () {
	}
	
	public void run() {
		List<String> cityList = dao.getAreaList();
		LoggerUtil.TaskInfoLog("[" + this.getClass().getSimpleName() + "][Start][CitySize " + cityList.size() + "]");
		List<Future<List<XiaoQuEntity>>> resultList = new ArrayList<Future<List<XiaoQuEntity>>>();
		listThreadPool = Executors.newFixedThreadPool(threadPoolSize/10);
		for (String eachCity : cityList) {
			String[] cityInfo = eachCity.split("_");
			String cityName = cityInfo[0];
			Map<Params, String> paramsMap = new HashMap<Params, String>();
			paramsMap.put(Params.area, cityName);
			paramsMap.put(Params.tempurl, tempUrl);
			paramsMap.put(Params.xiaoquType, String.valueOf(xiaoquType));
			paramsMap.put(Params.analysistype, analysisType.toString());
			DianPingListClawer listClawer = new DianPingListClawer(paramsMap);
			resultList.add(listThreadPool.submit(listClawer));
		}
		List<XiaoQuEntity> 
		int xiaoquSize = detailList.size();
		detailThreadPool = Executors.newFixedThreadPool(threadPoolSize);
		CountDownLatchUtils cdl = new CountDownLatchUtils(xiaoquSize);
		for (Future<List<XiaoQuEntity>> xiaoQuEntity : detailList) {
			BaseDetailClawer<XiaoQuEntity> detailClawer = new DianPingDetailClawer(xiaoQuEntity, cdl);
			detailThreadPool.execute(detailClawer);
		}
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LoggerUtil.TaskInfoLog("[" + this.getClass().getSimpleName() + "][Done][DetailSize " + xiaoquSize + "]");
		detailThreadPool.shutdownNow();
		detailThreadPool = null;
	}
	
	
}
