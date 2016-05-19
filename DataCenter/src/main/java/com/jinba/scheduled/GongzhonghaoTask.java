package com.jinba.scheduled;

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
import com.jinba.scheduled.gongzhonghao.GongzhonghaoDetailClawer;
import com.jinba.scheduled.gongzhonghao.GongzhonghaoListClawer;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

@Component
public class GongzhonghaoTask implements Runnable {

	@Autowired
	private MysqlDao dao;
	@Value("${gzhclaw.thread.pool}")
	private int threadPoolSize = 10;
	private ExecutorService listThreadPool;
	private ExecutorService detailThreadpool;
	private BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<Runnable>();
	private Logger logger = LoggerFactory.getLogger(GongzhonghaoListClawer.class);

	public GongzhonghaoTask () {
		listThreadPool = Executors.newFixedThreadPool(threadPoolSize);
		detailThreadpool  = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 60000, TimeUnit.MILLISECONDS, workQueue);
	}
	
	public void run() {
		List<String[]> cityList = dao.getGongzhonghaoList();
		int listSize = cityList.size();
		CountDownLatchUtils listCdl = new CountDownLatchUtils(listSize);
		List<Future<List<NewsEntity>>> resList = new ArrayList<Future<List<NewsEntity>>>();
		logger.info("[" + this.getClass().getSimpleName() + "][Start][CitySize " + cityList.size() + "]");
		for (String[] eachCity : cityList) {
			String gzhName = eachCity[1];
			String areaCode = eachCity[0];
			String xiaoquId = eachCity[2];
			Map<Params, String> paramsMap = new HashMap<Params, String>();
			paramsMap.put(Params.gongzhonghao, gzhName);
			paramsMap.put(Params.citycode, areaCode);
			paramsMap.put(Params.xiaoquid, xiaoquId);
			GongzhonghaoListClawer listClawer = new GongzhonghaoListClawer(paramsMap, listCdl);
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
				GongzhonghaoDetailClawer detail = new GongzhonghaoDetailClawer(newsEntity);
				detailThreadpool.execute(detail);
				logger.info("[" + this.getClass().getSimpleName() + "][Queue Size Is " + workQueue.size() + "]");
			}
		}
		logger.info("[" + this.getClass().getSimpleName() + "][Done]");
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		GongzhonghaoTask cookieTask = (GongzhonghaoTask) application.getBean("gongzhonghaoTask");
		cookieTask.run();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
