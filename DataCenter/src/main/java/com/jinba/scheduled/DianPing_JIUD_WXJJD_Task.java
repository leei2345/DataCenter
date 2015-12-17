package com.jinba.scheduled;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 酒店-五星级酒店
 * @author leei
 *
 */
@Component
public class DianPing_JIUD_WXJJD_Task extends DianPingTask {

	private static final String tempUrl = "http://www.dianping.com/@@/hotel/g3020p$$n10";
	private static final int xiaoquType = 3;
	
	public DianPing_JIUD_WXJJD_Task () {
		super(tempUrl, xiaoquType);
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		DianPingTask d = (DianPingTask) application.getBean("dianPing_JIUD_WXJJD_Task");
		d.run();
	}
	
}
