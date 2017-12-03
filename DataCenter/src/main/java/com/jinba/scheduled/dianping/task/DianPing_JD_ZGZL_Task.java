package com.jinba.scheduled.dianping.task;

import org.springframework.stereotype.Component;

import com.jinba.pojo.AnalysisType;

/**
 * 景点-展馆展览
 * @author leei
 *
 */
@Component
public class DianPing_JD_ZGZL_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/35/g2926p$$";;
	private static final int xiaoquType = 4;
	private static final AnalysisType analysisType = AnalysisType.dp_general;
	
	public DianPing_JD_ZGZL_Task () {
		super(tempUrl, xiaoquType, analysisType);
	}
	
	
}
