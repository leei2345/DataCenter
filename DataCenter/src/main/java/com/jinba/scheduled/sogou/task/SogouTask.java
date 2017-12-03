package com.jinba.scheduled.sogou.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.NewsEntity;
import com.jinba.scheduled.sogou.SogouDetailClawer;
import com.jinba.scheduled.sogou.SogouListClawer;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

@Component
public class SogouTask implements Runnable {

	@Autowired
	private MysqlDao dao;
	@Value("${sgclaw.thread.pool}")
	private int threadPoolSize = 10;
	private ExecutorService listThreadPool;
	private ExecutorService detailThreadpool;
	private BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<Runnable>();
	private Logger logger = LoggerFactory.getLogger(SogouListClawer.class);
	
	public void run() {
		List<String> cityList = dao.getAreaList(2,3,4);
		int listSize = cityList.size();
		CountDownLatchUtils listCdl = new CountDownLatchUtils(listSize);
		List<Future<List<NewsEntity>>> resList = new ArrayList<Future<List<NewsEntity>>>();
		logger.info("[" + this.getClass().getSimpleName() + "][Start][CitySize " + cityList.size() + "]");
		listThreadPool = Executors.newFixedThreadPool(threadPoolSize);
		detailThreadpool  = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 60000, TimeUnit.MILLISECONDS, workQueue);
		for (String eachCity : cityList) {
			String[] cityInfo = eachCity.split("_");
			String cityName = cityInfo[3];
			String areaCode = cityInfo[1];
			Map<Params, String> paramsMap = new HashMap<Params, String>();
			paramsMap.put(Params.area, cityName);
			paramsMap.put(Params.citycode, areaCode);
			SogouListClawer listClawer = new SogouListClawer(paramsMap, listCdl);
			resList.add(listThreadPool.submit(listClawer));
		}
		for (Future<List<NewsEntity>> future : resList) {
			List<NewsEntity> detailList;
			try {
				detailList = future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			} catch (ExecutionException e) {
				e.printStackTrace();
				continue;
			}
			for (NewsEntity newsEntity : detailList) {
				SogouDetailClawer detail = new SogouDetailClawer(newsEntity);
				detailThreadpool.execute(detail);
				logger.info("[" + this.getClass().getSimpleName() + "][Queue Size Is " + workQueue.size() + "]");
			}
		}
		logger.info("[" + this.getClass().getSimpleName() + "][Done]");
		listThreadPool.shutdownNow();
		listThreadPool = null;
		detailThreadpool.shutdownNow();
		detailThreadpool = null;
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		SogouCookieTask cookieTask = (SogouCookieTask) application.getBean("sogouCookieTask");
		cookieTask.run();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SogouTask a = (SogouTask) application.getBean("sogouTask");
		a.run();
	}

}
