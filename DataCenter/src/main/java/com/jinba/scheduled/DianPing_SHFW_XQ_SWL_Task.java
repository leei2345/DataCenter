package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 生活服务-小区/商务楼
 * @author leei
 *
 */
@Component
public class DianPing_SHFW_XQ_SWL_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/80/g5834/p$$";
	private static final int xiaoquType = 4;
	
	public DianPing_SHFW_XQ_SWL_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
