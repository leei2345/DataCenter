package com.jinba.scheduled;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * IT与互联网
 * @author zhangxiaolei
 *
 */
@Component
public class HDB_ITHLW_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-fl9r-sjbx-p$$/";
	private static final String PARTYTYPE = "J";
	
	public HDB_ITHLW_Task () {
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
