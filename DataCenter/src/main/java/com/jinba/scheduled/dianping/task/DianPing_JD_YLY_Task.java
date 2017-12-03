package com.jinba.scheduled.dianping.task;

import org.springframework.stereotype.Component;

import com.jinba.pojo.AnalysisType;

/**
 * 景点-游乐园
 * @author leei
 *
 */
@Component
public class DianPing_JD_YLY_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/35/g20045p$$";;
	private static final int xiaoquType = 4;
	private static final AnalysisType analysisType = AnalysisType.dp_general;

	public DianPing_JD_YLY_Task () {
		super(tempUrl, xiaoquType, analysisType);
	}
	
	
}
