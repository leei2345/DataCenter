package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 景点-古镇
 * @author leei
 *
 */
@Component
public class DianPing_JD_GZ_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/35/g2836p$$";;
	private static final int xiaoquType = 4;
	
	public DianPing_JD_GZ_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
