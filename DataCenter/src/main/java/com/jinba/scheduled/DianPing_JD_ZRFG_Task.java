package com.jinba.scheduled;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 景点-自然风光
 * @author leei
 *
 */
@Component
public class DianPing_JD_ZRFG_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/search/category/##/35/g32745p$$";;
	private static final int xiaoquType = 4;
	
	public DianPing_JD_ZRFG_Task () {
		super(tempUrl, xiaoquType);
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		DianPingTask d = (DianPingTask) application.getBean("dianPing_JD_ZRFG_Task");
		d.run();
	}
}
