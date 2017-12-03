package com.jinba.scheduled.hdb.task;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 派对与娱乐
 * @author Administrator
 *
 */
@Component
public class HDB_PDYL_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-fllj-sjbx-p$$/";
	private static final String PARTYTYPE = "D";
	
	public HDB_PDYL_Task () {
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
