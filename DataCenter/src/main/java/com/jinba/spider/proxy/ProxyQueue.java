package com.jinba.spider.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.ProxyCheckResEntity;

@Component
public class ProxyQueue {

	private static Map<Integer, ConcurrentLinkedQueue<ProxyCheckResEntity>> proxyCenter = new HashMap<Integer, ConcurrentLinkedQueue<ProxyCheckResEntity>>();
	
	private static MysqlDao dao;
	
	@Autowired
	public void setDao (MysqlDao dao) {
		ProxyQueue.dao = dao;
	}
	
	/**
	 * 从代理中心获取目标代理
	 * @param targetId 目标ID
	 * @return
	 */
	public static ProxyCheckResEntity getProxy (int targetId) {
		ConcurrentLinkedQueue<ProxyCheckResEntity> proxyList = proxyCenter.get(targetId);
		if (proxyList == null) {
			proxyList = new ConcurrentLinkedQueue<ProxyCheckResEntity>();
		}
		if (proxyList.isEmpty()) {
			refreshProxy(targetId, proxyList);
		}
		return proxyList.poll();
	}
	
	/**
	 * 刷新目标代理队列
	 * @param targetId
	 */
	public static void refreshProxy (int targetId, ConcurrentLinkedQueue<ProxyCheckResEntity> proxyList) {
		ConcurrentLinkedQueue<ProxyCheckResEntity> proxyQueue = dao.getProxyQueue(targetId);
		proxyList.addAll(proxyQueue);
		proxyCenter.put(targetId, proxyList);
	}
	
	
	
}
