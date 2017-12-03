package com.jinba.scheduled.dianping.task;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.jinba.pojo.AnalysisType;

/**
 * 教育培训-教育院校
 * @author leei
 *
 */
@Component
public class DianPing_JYPX_JYYX_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/75/g260p$$";
	private static final int xiaoquType = 4;
	private static final AnalysisType analysisType = AnalysisType.dp_educate;

	public DianPing_JYPX_JYYX_Task () {
		super(tempUrl, xiaoquType, analysisType);
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		DianPingTask d = (DianPingTask) application.getBean("dianPing_JYPX_JYYX_Task");
		d.run();
	}
}
