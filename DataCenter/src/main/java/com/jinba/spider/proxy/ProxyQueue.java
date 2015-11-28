package com.jinba.spider.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.ProxyCheckResEntity;

@Component
public class ProxyQueue {

	private static Map<Integer, LinkedBlockingDeque<ProxyCheckResEntity>> proxyCenter = new HashMap<Integer, LinkedBlockingDeque<ProxyCheckResEntity>>();
	@Resource
	private static MysqlDao dao;
	
	/**
	 * 从代理中心获取目标代理
	 * @param targetId 目标ID
	 * @return
	 */
	public static ProxyCheckResEntity getProxy (int targetId) {
		LinkedBlockingDeque<ProxyCheckResEntity> proxyList = proxyCenter.get(targetId);
		if (proxyList == null || proxyList.size() == 0) {
			refreshProxy(targetId);
		}
		return proxyList.poll();
	}
	
	/**
	 * 刷新目标代理队列
	 * @param targetId
	 */
	public static void refreshProxy (int targetId) {
		LinkedBlockingDeque<ProxyCheckResEntity> proxyQueue = dao.getProxyQueue(targetId);
		proxyCenter.put(targetId, proxyQueue);
	}
	
	
	
}
