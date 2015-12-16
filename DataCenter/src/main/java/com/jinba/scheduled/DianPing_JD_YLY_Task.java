package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 景点-游乐园
 * @author leei
 *
 */
@Component
public class DianPing_JD_YLY_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/35/g20045p$$";;
	private static final int xiaoquType = 4;
	
	public DianPing_JD_YLY_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
