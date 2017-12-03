package com.jinba.scheduled.hdb.task;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 亲子与幼教
 * @author Administrator
 *
 */
@Component
public class HDB_QZYJ_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-flzj-sjbx-p$$/";
	private static final String PARTYTYPE = "H";
	
	public HDB_QZYJ_Task () {
		super(TEMPURL, PARTYTYPE);
	}
	
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		HDBTask d = (HDBTask) application.getBean("HDB_JYXQ_Task");
		d.run();
	}

}
