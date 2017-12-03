package com.jinba.scheduled.hdb.task;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 体验与促销
 * @author zhangxiaolei
 *
 */
@Component
public class HDB_TYCX_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-fl4j-sjbx-p$$/";
	private static final String PARTYTYPE = "F";
	
	public HDB_TYCX_Task () {
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
