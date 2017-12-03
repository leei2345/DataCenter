package com.jinba.scheduled.hdb.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jinba.pojo.PartyEntity;
import com.jinba.scheduled.hdb.HDBDetailClawer;
import com.jinba.scheduled.hdb.HdbCityMap;
import com.jinba.scheduled.hdb.HdbListClawer;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

@Component
public class HDBTask implements Runnable {

	@Value("${hdbclaw.thread.pool}")
	private int threadPoolSize = 2;
	private ExecutorService listThreadPool;
	private ExecutorService detailThreadpool;
	private BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<Runnable>();
	private String tempUrl;
	private String partyType;
	private Logger logger = LoggerFactory.getLogger(HdbListClawer.class);
	
	public HDBTask () {}
	
	public HDBTask (String tempUrl, String partyType) {
		this.partyType = partyType;
		this.tempUrl = tempUrl;
	}
	
	public void run() {
		Set<String> cityList = HdbCityMap.getCityList();
		
//		Set<String> cityList = new HashSet<String>();
//		cityList.add("beijing");
		
		
		int listSize = cityList.size();
		CountDownLatchUtils listCdl = new CountDownLatchUtils(listSize);
		List<Future<List<PartyEntity>>> resList = new ArrayList<Future<List<PartyEntity>>>();
		logger.info("[" + this.getClass().getSimpleName() + "][Start][CitySize " + cityList.size() + "]");
		listThreadPool = Executors.newFixedThreadPool(threadPoolSize);
		detailThreadpool  = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 60000, TimeUnit.MILLISECONDS, workQueue);
		for (String eachCity : cityList) {
			Map<Params, String> paramsMap = new HashMap<Params, String>();
			paramsMap.put(Params.area, eachCity);
			paramsMap.put(Params.tempurl, tempUrl);
			paramsMap.put(Params.parttype, partyType);
			HdbListClawer listClawer = new HdbListClawer(paramsMap, listCdl);
			resList.add(listThreadPool.submit(listClawer));
		}
		for (Future<List<PartyEntity>> future : resList) {
			List<PartyEntity> detailList;
			try {
				detailList = future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			} catch (ExecutionException e) {
				e.printStackTrace();
				continue;
			}
			for (PartyEntity partyEntity : detailList) {
				HDBDetailClawer detail = new HDBDetailClawer(partyEntity);
				detailThreadpool.execute(detail);
				logger.info("[" + this.getClass().getSimpleName() + "][Queue Size Is " + workQueue.size() + "]");
			}
		}
		logger.info("[" + this.getClass().getSimpleName() + "][Done]");
		listThreadPool.shutdownNow();
		listThreadPool = null;
	}
	

}
