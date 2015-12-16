package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 酒店-五星级酒店
 * @author leei
 *
 */
@Component
public class DianPing_JIUD_WXJJD_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/20/g119/p$$";
	private static final int xiaoquType = 3;
	
	public DianPing_JIUD_WXJJD_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}