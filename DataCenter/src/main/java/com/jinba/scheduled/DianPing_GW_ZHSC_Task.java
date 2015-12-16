package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 购物-综合商场
 * @author leei
 *
 */
@Component
public class DianPing_GW_ZHSC_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/20/g119/p$$";
	private static final int xiaoquType = 3;
	
	public DianPing_GW_ZHSC_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
