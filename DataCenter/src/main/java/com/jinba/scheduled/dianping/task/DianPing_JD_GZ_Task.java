package com.jinba.scheduled.dianping.task;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.jinba.pojo.AnalysisType;

/**
 * 景点-古镇
 * @author leei
 *
 */
@Component
public class DianPing_JD_GZ_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/35/g2836p$$";;
	private static final int xiaoquType = 4;
	private static final AnalysisType analysisType = AnalysisType.dp_general;
	
	public DianPing_JD_GZ_Task () {
		super(tempUrl, xiaoquType, analysisType);
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		DianPingTask d = (DianPingTask) application.getBean("dianPing_JD_GZ_Task");
		d.run();
	}
	
}
