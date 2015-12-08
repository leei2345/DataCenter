package com.jinba.scheduled.dianping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.jinba.pojo.XiaoQuEntity;
import com.jinba.scheduled.BaseDetailClawer;
import com.jinba.scheduled.DBHandle;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 */
public class DianPingDetailClawer extends BaseDetailClawer<XiaoQuEntity>{

	private static final int TARGETID = 1;
	
	public DianPingDetailClawer(XiaoQuEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
	}

	@Override
	protected int initParams() {
		return 0;
	}

	@Override
	protected String getDetailHtml() {
		String sourceKey = detailEntity.getFormkey();
		String url = "http://www.dianping.com/shop/" + sourceKey;
		String html = httpGet(url);
		return html;
	}

	@Override
	protected void analysistDetail(String html, DBHandle dbHandle) {
		Document doc = Jsoup.parse(html);
		String areaName = doc.select("div[class=expand-info address] > a[rel=nofollow] > span").text().trim();
		areaName = areaName.replace("其他", "");
		String areaCode = DianPingCityMap.
		
		
	}


}
