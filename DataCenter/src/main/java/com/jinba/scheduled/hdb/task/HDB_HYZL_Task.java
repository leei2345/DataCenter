package com.jinba.scheduled.hdb.task;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 会议与展览
 * @author zhangxiaolei
 *
 */
@Component
public class HDB_HYZL_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-flqj-sjbx-p$$/";
	private static final String PARTYTYPE = "I";
	
	public HDB_HYZL_Task () {
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
