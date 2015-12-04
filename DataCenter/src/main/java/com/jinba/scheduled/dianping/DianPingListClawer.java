package com.jinba.scheduled.dianping;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.jinba.pojo.XiaoQuEntity;
import com.jinba.scheduled.BaseListClawer;
import com.jinba.spider.core.Params;

/**
 * 
 * @author leei
 *
 */
public class DianPingListClawer extends BaseListClawer<XiaoQuEntity> implements Runnable {

	private static final int TARGETID = 1;
	private String eachPageUrl;
	private int pageCount = 1;
	
	public DianPingListClawer () {
		super(TARGETID);
	}
	
	@Override
	protected int initParams(Map<Params, String> paramsMap) {
		String city = paramsMap.get(Params.area);
		String cityCode = DianPingCityMap.getCityCode(city);
		if (StringUtils.isBlank(cityCode)) {
			return INITFAIL;
		}
		String tempUrl = paramsMap.get(Params.tempurl);
		eachPageUrl = tempUrl.replace("##", cityCode);
		String page1Url = eachPageUrl.replace("$$", "1");
		String page1Html = httpGet(page1Url);
		Document doc = Jsoup.parse(page1Html);
		Elements lastPageNode = doc.select("div.page>a:nth-last-of-type(+1)");
		String pageCountStr = lastPageNode.text();
		try {
			pageCount = Integer.parseInt(pageCountStr);
		} catch (Exception e) {
		}
		return INITSUCC;
	}

	@Override
	protected List<XiaoQuEntity> analysisAction(List<XiaoQuEntity> box) {
		for (int pageIndex = 1; pageIndex <= pageCount; pageIndex++) {
			String url = eachPageUrl.replace("$$", String.valueOf(pageIndex));
			String html = httpGet(url);
			Document doc = Jsoup.parse(html);
			Elements nodes = doc.select("div#shop-all-list > ul > li");
			XiaoQuEntity x = new XiaoQuEntity();
			
			
			
		}
		
		
		return null;
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}

}
