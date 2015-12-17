package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 酒店-度假村
 * @author leei
 *
 */
@Component
public class DianPing_JIUD_DJC_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/@@/hotel/g173p$$n10";
	private static final int xiaoquType = 3;
	
	public DianPing_JIUD_DJC_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
