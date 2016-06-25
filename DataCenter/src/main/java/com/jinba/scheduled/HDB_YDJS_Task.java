package com.jinba.scheduled;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 运动与健身
 * @author zhangxiaolei
 *
 */
@Component
public class HDB_YDJS_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-fl8j-sjbx-p$$/";
	private static final String PARTYTYPE = "B";
	
	public HDB_YDJS_Task () {
		super(TEMPURL, PARTYTYPE);
	}
	
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		HDBTask d = (HDBTask) application.getBean("HDB_HWYD_Task");
		d.run();
	}

}
