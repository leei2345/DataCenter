package com.jinba.scheduled;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class HDB_KCJY_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-flj-sjbx-p$$/";
	private static final String PARTYTYPE = "G";
	
	public HDB_KCJY_Task () {
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
