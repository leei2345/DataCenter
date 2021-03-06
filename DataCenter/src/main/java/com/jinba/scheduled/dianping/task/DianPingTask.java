package com.jinba.scheduled.dianping.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.AnalysisType;
import com.jinba.scheduled.dianping.DianPingListClawer;
import com.jinba.scheduled.sogou.task.SogouCookieTask;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

@Component
public class DianPingTask implements Runnable {

	private String tempUrl;
	private int xiaoquType;
	private AnalysisType analysisType;
	@Resource
	private MysqlDao dao;
	@Value("${dpclaw.thread.pool}")
	private int threadPoolSize = 30;
	private ExecutorService listThreadPool;
	private Logger logger = LoggerFactory.getLogger(DianPingListClawer.class);
	
	public DianPingTask (String tempUrl, int xiaoquType, AnalysisType analysisType) {
		this.tempUrl = tempUrl;
		this.xiaoquType = xiaoquType;
		this.analysisType = analysisType;
	}
	
	public DianPingTask () {
	}
	
	public void run() {
		List<String> cityList = dao.getAreaList(2,3);
		int listSize = cityList.size();
		CountDownLatchUtils listCdl = new CountDownLatchUtils(listSize);
		logger.info("[" + this.getClass().getSimpleName() + "][Start][CitySize " + cityList.size() + "]");
		listThreadPool = Executors.newFixedThreadPool(threadPoolSize/10);
		for (String eachCity : cityList) {
			String[] cityInfo = eachCity.split("_");
			String cityName = cityInfo[3];
			Map<Params, String> paramsMap = new HashMap<Params, String>();
			paramsMap.put(Params.area, cityName);
			paramsMap.put(Params.tempurl, tempUrl);
			paramsMap.put(Params.xiaoquType, String.valueOf(xiaoquType));
			paramsMap.put(Params.analysistype, analysisType.toString());
			DianPingListClawer listClawer = new DianPingListClawer(paramsMap, listCdl);
			listThreadPool.execute(listClawer);
		}
		try {
			listCdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("[" + this.getClass().getSimpleName() + "][Done]");
		listThreadPool.shutdownNow();
		listThreadPool = null;
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[] { "database.xml" });
		application.start();
		SogouCookieTask a = (SogouCookieTask) application.getBean("sogouCookieTask");
		a.run();
	}
	
	
}
