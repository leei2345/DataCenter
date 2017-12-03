package com.jinba.scheduled.dianping.task;

import org.springframework.stereotype.Component;

import com.jinba.pojo.AnalysisType;

/**
 * 酒店-三星级酒店
 * @author leei
 *
 */
@Component
public class DianPing_JIUD_3XJJD_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/@@/hotel/g3024p$$n10";
	private static final int xiaoquType = 3;
	private static final AnalysisType analysisType = AnalysisType.dp_hotel;
	
	public DianPing_JIUD_3XJJD_Task () {
		super(tempUrl, xiaoquType, analysisType);
	}
	
	
}
