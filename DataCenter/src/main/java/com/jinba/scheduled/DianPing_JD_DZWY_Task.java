package com.jinba.scheduled;

import org.springframework.stereotype.Component;

import com.jinba.pojo.AnalysisType;

/**
 * 景点-动植物园
 * @author leei
 *
 */
@Component
public class DianPing_JD_DZWY_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/35/g2834p$$";;
	private static final int xiaoquType = 4;
	private static final AnalysisType analysisType = AnalysisType.dp_general;
	
	public DianPing_JD_DZWY_Task () {
		super(tempUrl, xiaoquType, analysisType);
	}
	
	
}
