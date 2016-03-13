package com.jinba.scheduled;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 职场干货
 * @author zhangxiaolei
 *
 */
@Component
public class HDB_ZCGH_Task extends HDBTask {

	private static final String TEMPURL = "http://www.hdb.com/find/@@-flt-sjbx-p$$/";
	private static final String PARTYTYPE = "J";
	
	public HDB_ZCGH_Task () {
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
