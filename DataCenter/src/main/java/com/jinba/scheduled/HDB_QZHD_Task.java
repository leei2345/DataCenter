package com.jinba.scheduled;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 亲子互动
 * @author zhangxiaolei
 *
 */
@Component
public class HDB_QZHD_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-fl7-sjbx-p$$/";
	private static final String PARTYTYPE = "H";
	
	public HDB_QZHD_Task () {
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
