package com.jinba.scheduled.hdb.task;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 公益与慈善
 * @author zhangxiaolei
 *
 */
@Component
public class HDB_GYCS_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-flgj-sjbx-p$$/";
	private static final String PARTYTYPE = "C";
	
	public HDB_GYCS_Task () {
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
