package com.jinba.scheduled;

import org.springframework.stereotype.Component;

/**
 * 景点-展馆展览
 * @author leei
 *
 */
@Component
public class DianPing_JD_ZGZL_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/35/g2926/P$$";;
	private static final int xiaoquType = 4;
	
	public DianPing_JD_ZGZL_Task () {
		super(tempUrl, xiaoquType);
	}
	
	
}
