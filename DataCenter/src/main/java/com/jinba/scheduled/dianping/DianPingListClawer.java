package com.jinba.scheduled.dianping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
	private int xiaoquType;
	
	public DianPingListClawer (Map<Params, String> paramsMap) {
		super(TARGETID);
		this.paramsMap = paramsMap;
	}
	
	@Override
	protected int initParams() {
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
		Elements lastPageNode = doc.select("div.page>a:nth-last-of-type(+2)");
		String pageCountStr = lastPageNode.text();
		try {
			pageCount = Integer.parseInt(pageCountStr);
		} catch (Exception e) {
		}
		xiaoquType = Integer.parseInt(paramsMap.get(Params.xiaoquType));
		return INITSUCC;
	}

	@Override
	protected void analysisAction(List<XiaoQuEntity> box) {
		for (int pageIndex = 1; pageIndex <= pageCount; pageIndex++) {
			String url = eachPageUrl.replace("$$", String.valueOf(pageIndex));
			String html = httpGet(url);
			Document doc = Jsoup.parse(html, url);
			Elements nodes = doc.select("div#shop-all-list > ul > li");
			for (Element node : nodes) {
				XiaoQuEntity x = new XiaoQuEntity();
				x.setXiaoquType(xiaoquType);
				String headPhotoUrl = node.select("div.pic > a > img").attr("data-src").trim();
				if (!StringUtils.isBlank(headPhotoUrl)) {
					x.setHeading(headPhotoUrl);
				}		
				String xiaoquName = node.select("div.tit > a > h4").text().trim();
				if (StringUtils.isBlank(xiaoquName)) {
					continue;
				}
				x.setXiaoquname(xiaoquName);
				String sourceUrl = node.select("div.pic > a").attr("abs:href").trim();
				if (StringUtils.isBlank(sourceUrl)) {
					continue;
				}
				x.setFromurl(sourceUrl);
				x.setFormkey(sourceUrl.replaceAll("\\D+", ""));
				box.add(x);
			}
		}
	}

	public void run() {
		this.claw();
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		Map<Params, String> paramsMap = new HashMap<Params, String>();
		paramsMap.put(Params.tempurl, "http://www.dianping.com/search/category/##/35/g2901/p$$");
		paramsMap.put(Params.area, "北京市");
		paramsMap.put(Params.xiaoquType, "4");
		new Thread(new DianPingListClawer(paramsMap)).start();
	}

}
