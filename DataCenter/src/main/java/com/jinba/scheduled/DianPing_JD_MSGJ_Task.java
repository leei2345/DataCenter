package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 景点-名胜古迹
 * @author leei
 *
 */
@Component
public class DianPing_JD_MSGJ_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/35/g2901/P$$";;
	private static final int xiaoquType = 4;
	
	public DianPing_JD_MSGJ_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
