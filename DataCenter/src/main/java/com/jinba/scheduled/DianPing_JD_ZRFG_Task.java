package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 景点-自然风光
 * @author leei
 *
 */
@Component
public class DianPing_JD_ZRFG_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/35/g32745/P$$";;
	private static final int xiaoquType = 4;
	
	public DianPing_JD_ZRFG_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
