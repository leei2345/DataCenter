package com.jinba.scheduled;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.jinba.pojo.AnalysisType;

/**
 * 购物-综合商场
 * @author leei
 *
 */
@Component
public class DianPing_GW_ZHSC_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/20/g119p$$";
	private static final int xiaoquType = 3;
	private static final AnalysisType analysisType = AnalysisType.dp_trade;
	
	public DianPing_GW_ZHSC_Task () {
		super(tempUrl, xiaoquType, analysisType);
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		DianPingTask d = (DianPingTask) application.getBean("dianPing_GW_ZHSC_Task");
		d.run();
	
	}
	
	
}
