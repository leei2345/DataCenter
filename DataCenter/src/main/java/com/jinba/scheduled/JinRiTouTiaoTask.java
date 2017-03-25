package com.jinba.scheduled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.NewsEntity;
import com.jinba.scheduled.jinritoutiao.JinRiTouTiaoDetailClawer;
import com.jinba.scheduled.jinritoutiao.JinRiTouTiaoListClawer;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

@Component
public class JinRiTouTiaoTask implements Runnable {

	@Autowired
	private MysqlDao dao;
	@Value("${sgclaw.thread.pool}")
	private int threadPoolSize = 30;
	private ExecutorService listThreadPool;
	private Logger logger = LoggerFactory.getLogger(JinRiTouTiaoTask.class);
	
	public void run() {
		List<String> cityList = dao.getAreaList(2,3,4);
//		cityList.clear();cityList.add("中关村_11010801");
		int listSize = cityList.size();
		CountDownLatchUtils listCdl = new CountDownLatchUtils(listSize);
		List<Future<List<NewsEntity>>> resList = new ArrayList<Future<List<NewsEntity>>>();
		logger.info("[" + this.getClass().getSimpleName() + "][Start][CitySize " + cityList.size() + "]");
		listThreadPool = Executors.newFixedThreadPool(threadPoolSize);
		for (String eachCity : cityList) {
			String[] cityInfo = eachCity.split("_");
			String cityName = cityInfo[0];
			String areaCode = cityInfo[1];
			Map<Params, String> paramsMap = new HashMap<Params, String>();
			paramsMap.put(Params.area, cityName);
			paramsMap.put(Params.citycode, areaCode);
			JinRiTouTiaoListClawer listClawer = new JinRiTouTiaoListClawer(paramsMap, listCdl);
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
				JinRiTouTiaoDetailClawer detail = new JinRiTouTiaoDetailClawer(newsEntity);
				detail.detailAction();
			}
		}
		logger.info("[" + this.getClass().getSimpleName() + "][Done]");
		listThreadPool.shutdownNow();
		listThreadPool = null;
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		JinRiTouTiaoTask a = (JinRiTouTiaoTask) application.getBean("JinRiTouTiaoTask");
		a.run();
	}
	

}
