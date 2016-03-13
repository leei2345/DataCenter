package com.jinba.scheduled;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class HDB_JYXQ_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-fl3-sjbx-p$$/";
	private static final String PARTYTYPE = "D";
	
	public HDB_JYXQ_Task () {
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
