package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 教育培训-教育院校
 * @author leei
 *
 */
@Component
public class DianPing_JYPX_JYYX_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/75/g260p$$";
	private static final int xiaoquType = 4;
	
	public DianPing_JYPX_JYYX_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
