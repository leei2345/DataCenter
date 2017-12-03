package com.jinba.scheduled.dianping.task;

import org.springframework.stereotype.Component;

import com.jinba.pojo.AnalysisType;

/**
 * 酒店-经济型酒店
 * @author leei
 *
 */
@Component
public class DianPing_JIUD_JJXJD_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/@@/hotel/g171p$$n10";
	private static final int xiaoquType = 3;
	private static final AnalysisType analysisType = AnalysisType.dp_hotel;

	public DianPing_JIUD_JJXJD_Task () {
		super(tempUrl, xiaoquType, analysisType);
	}
	
	
}
