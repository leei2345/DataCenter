package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 生活服务-医院
 * @author leei
 *
 */
@Component
public class DianPing_SHFW_YY_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/80/g181/p$$";
	private static final int xiaoquType = 4;
	
	public DianPing_SHFW_YY_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
