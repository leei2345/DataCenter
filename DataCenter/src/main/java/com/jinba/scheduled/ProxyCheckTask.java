package com.jinba.scheduled;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
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
public class ProxyCheckTask implements Runnable {
	
	@Value("${check.thread.pool}")
	private int threadPoolSize = 2;
	private ExecutorService threadPool;
	@Resource
	private MysqlDao dao;
	private static final String IDENTIDY = "ProxyCheck";
	private TargetEntity target;
    
	public ProxyCheckTask () {}
	
	public ProxyCheckTask (TargetEntity target) {
		this.target = target;
		threadPool = Executors.newFixedThreadPool(threadPoolSize);
	}
	
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	/**
	 * 定时任务主方法，方便扩展为异步方式
	 */
	public void run() {
		Multiset<String> proxySourceSet = MysqlDao.getInstance().getTargetProxyCount(-1);
		int targetId = target.getId();
		/** 查看目标代理是否已经在代理表中存在 */
		Multiset<String> targetProxySet = MysqlDao.getInstance().getTargetProxyCount(targetId);
		if (!targetProxySet.containsAll(proxySourceSet)) {
			for (String str : proxySourceSet.elementSet()) {
				if (targetProxySet.contains(str)) {
					continue;
				}
				String host = str.split(":")[0];
				String port = str.split(":")[1];
				try {
					MysqlDao.getInstance().insertProxyToAvail(host, port, targetId);
				} catch (Exception e) {
					continue;
				}
				LoggerUtil.ProxyLog("[Add Proxy][" + targetId + "][" + str + "]");
			}
		}
		targetProxySet.removeAll(proxySourceSet);
		for (String proxy : targetProxySet.elementSet()) {
			MysqlDao.getInstance().removeProxy(proxy, targetId);
			LoggerUtil.ProxyLog("[Remove Proxy][" + targetId + "][" + proxy + "]");
		}
		List<ProxyCheckResEntity> proxyList = MysqlDao.getInstance().getNeedCheckProxy(targetId);
		CountDownLatchUtils cdl = new CountDownLatchUtils(proxyList.size());
		LoggerUtil.ProxyLog("[" + IDENTIDY + "][" + targetId + "][Start]");
		for (ProxyCheckResEntity proxyCheckResEntity : proxyList) {
			ProxyChecker checker = new ProxyChecker();
			checker.setTarget(target).setProxy(proxyCheckResEntity).setCdl(cdl);
			threadPool.execute(checker);
		}
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LoggerUtil.ProxyLog("[" + IDENTIDY + "][" + targetId + "][Done]");
	}
	
	public void startCheck () {
		Map<Integer, TargetEntity> targetMap = dao.getTargetMap();
		for (Entry<Integer, TargetEntity> map : targetMap.entrySet()) {
			ProxyCheckTask proxyChecker = new ProxyCheckTask(map.getValue());
			new Thread(proxyChecker).start();
		}
		
	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		ProxyCheckTask a = (ProxyCheckTask) application.getBean("proxyCheckTask");
		a.startCheck();
	}

	
}
