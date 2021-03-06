package com.jinba.scheduled.dianping.task;

import org.springframework.stereotype.Component;

import com.jinba.pojo.AnalysisType;

/**
 * 生活服务-医院
 * @author leei
 *
 */
@Component
public class DianPing_SHFW_YY_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/80/g181p$$";
	private static final int xiaoquType = 4;
	private static final AnalysisType analysisType = AnalysisType.dp_general;

	public DianPing_SHFW_YY_Task () {
		super(tempUrl, xiaoquType, analysisType);
	}
	
	
}
