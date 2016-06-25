package com.jinba.scheduled;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 创业与投资
 * @author Administrator
 *
 */
@Component
public class HDB_CYTZ_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-fla8-sjbx-p$$/";
	private static final String PARTYTYPE = "J";
	
	public HDB_CYTZ_Task () {
		super(TEMPURL, PARTYTYPE);
	}
	
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		HDBTask d = (HDBTask) application.getBean("HDB_CYTZ_Task");
		d.run();
	}

}
