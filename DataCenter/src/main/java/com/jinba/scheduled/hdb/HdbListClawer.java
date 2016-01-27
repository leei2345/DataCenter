package com.jinba.scheduled.hdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jinba.core.BaseClawer;
import com.jinba.core.BaseListClawer;
import com.jinba.core.BaseClawer.ActionRes;
import com.jinba.pojo.AnalysisType;
import com.jinba.pojo.PartyEntity;
import com.jinba.pojo.XiaoQuEntity;
import com.jinba.scheduled.AreaInfoMap;
import com.jinba.scheduled.DianPingWorker;
import com.jinba.scheduled.dianping.DianPingCityMap;
import com.jinba.scheduled.dianping.DianPingListClawer;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.ImageClawer;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

public class HdbListClawer extends BaseListClawer<XiaoQuEntity> implements Runnable {

	private static final int TARGETID = 4;
	private static final String TARGETINFO = "hdb";
	private String eachPageUrl;
	private int pageCount = 1;
	private String partType;
	private String areaCode;
	private static final String FROMHOST = "www.hdb.com";
	private static final String IMAGEDIRNAME = "party";
	
	public HdbListClawer (Map<Params, String> paramsMap, CountDownLatchUtils cdl) {
		super(TARGETID, cdl);
		this.paramsMap = paramsMap;
	}
	
	@Override
	protected ActionRes initParams() {
		String cityEnCode = paramsMap.get(Params.sourcecitycode);
		String ownCityCode = HdbCityMap.getAreaCode(cityEnCode);
		if (StringUtils.isBlank(ownCityCode) ||  StringUtils.isBlank(cityEnCode)) {
			return ActionRes.ANALYSIS_FAIL;
		}
		areaCode = ownCityCode;
		String tempUrl = paramsMap.get(Params.tempurl);
		eachPageUrl = tempUrl.replace("@@", cityEnCode);
		partType = paramsMap.get(Params.parttype);
		paramsMap = null;
		return ActionRes.INITSUCC;
	}

	@Override
	protected void analysisAction(List<XiaoQuEntity> box) {
		boolean hasNext = false;
		int pageIndex = 1;
		do {
			String url = eachPageUrl.replace("$$", String.valueOf(pageIndex));
			HttpMethod m = new HttpMethod(TARGETID);
			String html = m.GetHtml(url, HttpResponseConfig.ResponseAsStream);
			Document doc = Jsoup.parse(html, url);
			Elements nodes = doc.select("div.find_main > ul.find_main_ul > li");
			for (Element node : nodes) {
				PartyEntity x = new PartyEntity();
				x.setParttype(partType);
				x.setFromhost(FROMHOST);
				
				String title = node.select("div.find_main_div > div.find_main_title> a > h4").text().trim();
				if (StringUtils.isBlank(title)) {
					continue;
				}
				x.setTitle(title);
				String areaName = node.select("div.find_main_address > p> a").text().trim();
				String ownAreaCode = null;
				if (!StringUtils.isBlank(areaName)) {
						ownAreaCode = AreaInfoMap.getAreaCode(areaName, areaCode);
				}
				if (StringUtils.isBlank(ownAreaCode)) {
					x.setAreacode(areaCode);
				} else {
					x.setAreacode(ownAreaCode);
				}
				String sourceUrl = node.select("div.find_main_div > div.find_main_title> a").attr("href").trim();
				if (StringUtils.isBlank(sourceUrl)) {
					continue;
				}
				x.setFromurl(sourceUrl);
				String fromKey = sourceUrl.replaceAll("\\D+", "");
				x.setFromkey(fromKey);
				ImageClawer imageClawer = new ImageClawer(headPhotoUrl, TARGETID, TARGETINFO, cityInfo.get(Params.cityname) ,fromKey, IMAGEDIRNAME);
				ImageClawer.ExecutorClaw(imageClawer);
				box.add(x);
			}
		} while (hasNext && pageIndex < 10);
	}
	
	public void run() {
		List<XiaoQuEntity> list = this.listAction();
//		for (XiaoQuEntity xiaoQuEntity : list) {
//			System.out.println(xiaoQuEntity.getCityInfo().get(Params.cityname) + "," + xiaoQuEntity.getFromkey());
//		}
		DianPingWorker.getInstance().offerWork(list);
		list = null;
	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		Map<Params, String> paramsMap = new HashMap<Params, String>();
		paramsMap.put(Params.tempurl, "http://www.dianping.com/search/category/##/35/g2836p$$");
		paramsMap.put(Params.area, "滨海县");
		paramsMap.put(Params.xiaoquType, "4");
		paramsMap.put(Params.analysistype, AnalysisType.dp_educate.toString());
		try {
			new DianPingListClawer(paramsMap, new CountDownLatchUtils(1)).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
