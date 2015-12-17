package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 酒店-经济型酒店
 * @author leei
 *
 */
@Component
public class DianPing_JIUD_JJXJD_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/@@/hotel/g171p$$n10";
	private static final int xiaoquType = 3;
	
	public DianPing_JIUD_JJXJD_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
