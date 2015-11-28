package com.jinba.scheduled;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multiset;
import com.jinba.dao.MysqlDao;
import com.jinba.pojo.ProxyCheckResEntity;
import com.jinba.pojo.TargetEntity;
import com.jinba.spider.proxy.ProxyChecker;
import com.jinba.utils.CountDownLatchUtils;
import com.jinba.utils.LoggerUtil;

/**
 * 代理检测定时任务
 * @author leei
 *
 */
@Component
public class ProxyCheckTask implements Runnable, ApplicationContextAware{
	
	@Value("${check.thread.pool}")
	private int threadPoolSize = 3;
	private static ExecutorService threadPool;
	@Resource
	private MysqlDao dao;
	private static final String IDENTIDY = "ProxyCheck";
    private ApplicationContext context;
	
	public ProxyCheckTask () {
		threadPool = Executors.newFixedThreadPool(threadPoolSize);
	}
	
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	/**
	 * 定时任务主方法，方便扩展为异步方式
	 */
	public void run() {
		/** 首先获取要检测的目标列表 **/
		Map<Integer, TargetEntity> targetMap = dao.getTargetMap();
		Multiset<String> proxySourceSet = dao.getTargetProxyCount(-1);
		for (Entry<Integer, TargetEntity> entry : targetMap.entrySet()) {
			int targetId = entry.getKey();
			/** 查看目标代理是否已经在代理表中存在 */
			Multiset<String> targetProxySet = dao.getTargetProxyCount(targetId);
			if (!targetProxySet.containsAll(proxySourceSet)) {
				for (String str : proxySourceSet.elementSet()) {
					if (targetProxySet.contains(str)) {
						continue;
					}
					String host = str.split(":")[0];
					String port = str.split(":")[1];
					dao.insertProxyToAvail(host, port, targetId);
				}
			}
			TargetEntity targetEntity = entry.getValue();
			List<ProxyCheckResEntity> proxyList = dao.getNeedCheckProxy(targetId);
			CountDownLatchUtils cdl = new CountDownLatchUtils(proxyList.size());
			LoggerUtil.ProxyLog("[" + IDENTIDY + "][" + targetId + "][Start]");
			for (ProxyCheckResEntity proxyCheckResEntity : proxyList) {
				ProxyChecker checker = (ProxyChecker) this.context.getBean("proxyChecker");
				checker.setTarget(targetEntity).setProxy(proxyCheckResEntity).setCdl(cdl);
				threadPool.execute(checker);
			}
			try {
				cdl.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			LoggerUtil.ProxyLog("[" + IDENTIDY + "][" + targetId + "][Done]");
		}
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		ProxyCheckTask a = (ProxyCheckTask) application.getBean("proxyCheckTask");
		a.run();
	}

	public void setApplicationContext(ApplicationContext paramApplicationContext) throws BeansException {
		this.context = (ApplicationContext)paramApplicationContext;    		
	}
	
}
