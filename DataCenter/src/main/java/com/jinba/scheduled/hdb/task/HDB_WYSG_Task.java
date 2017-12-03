package com.jinba.scheduled.hdb.task;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 文艺与手工
 * @author Administrator
 *
 */
@Component
public class HDB_WYSG_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-flyj-sjbx-p$$/";
	private static final String PARTYTYPE = "A";
	
	public HDB_WYSG_Task () {
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
