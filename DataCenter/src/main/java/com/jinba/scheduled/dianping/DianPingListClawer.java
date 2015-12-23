package com.jinba.scheduled.dianping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jinba.core.BaseListClawer;
import com.jinba.pojo.AnalysisType;
import com.jinba.pojo.XiaoQuEntity;
import com.jinba.spider.core.Params;

/**
 * 
 * @author leei
 *
 */
public class DianPingListClawer extends BaseListClawer<XiaoQuEntity> implements Callable<List<XiaoQuEntity>> {

	private static final int TARGETID = 1;
	private static final String TARGETINFO = "dianping";
	private String eachPageUrl;
	private int pageCount = 1;
	private int xiaoquType;
	private AnalysisType analysisType;
	private Map<Params, String> cityInfo = new HashMap<Params, String>();
	private static final String FROMHOST = "www.dianping.com";
	private static final String IMAGEDIRNAME = "shop";
	
	public DianPingListClawer (Map<Params, String> paramsMap) {
		super(TARGETID);
		this.paramsMap = paramsMap;
	}
	
	@Override
	protected ActionRes initParams() {
		String city = paramsMap.get(Params.area);
		String cityNumCode = DianPingCityMap.getCityNumCode(city);
		String cityEnCode = DianPingCityMap.getCityEnCode(city);
		String ownCityCode = DianPingCityMap.getAreaCode(city);
		if (StringUtils.isBlank(cityNumCode) ||  StringUtils.isBlank(cityEnCode)) {
			return ActionRes.ANALYSIS_FAIL;
		}
		cityInfo.put(Params.cityname, city);
		cityInfo.put(Params.citycode, ownCityCode);
		analysisType = AnalysisType.valueOf(paramsMap.get(Params.analysistype));
		
		String tempUrl = paramsMap.get(Params.tempurl);
		eachPageUrl = tempUrl.replace("@@", cityEnCode).replace("##", cityNumCode);
		xiaoquType = Integer.parseInt(paramsMap.get(Params.xiaoquType));
		return ActionRes.INITSUCC;
	}

	@Override
	protected void analysisAction(List<XiaoQuEntity> box) {
		for (int pageIndex = 1; pageIndex <= pageCount; pageIndex++) {
			String url = eachPageUrl.replace("$$", String.valueOf(pageIndex));
			String html = httpGet(url);
			Document doc = Jsoup.parse(html, url);
			if (pageIndex == 1) {
				Elements lastPageNode = doc.select("div.page>a:nth-last-of-type(+2)");
				String pageCountStr = lastPageNode.text();
				try {
					pageCount = Integer.parseInt(pageCountStr);
				} catch (Exception e) {
				}
			}
			Elements nodes = doc.select("div.content > div#shop-all-list > ul > li");
			if (AnalysisType.dp_hotel.equals(analysisType)) {
				nodes = doc.select("div.content > ul.hotelshop-list > li");
			}
			for (Element node : nodes) {
				XiaoQuEntity x = new XiaoQuEntity();
				x.setXiaoquType(xiaoquType);
				x.setAnalysisType(analysisType);
				x.setCityInfo(cityInfo);
				x.setFromhost(FROMHOST);
				String headPhotoUrl = node.select("div.pic > a > img").attr("data-src").trim();
				if (AnalysisType.dp_hotel.equals(analysisType)) {
					try {
						Element photoNode = node.select("div.hotel-pics > ul > li").first();
						headPhotoUrl = photoNode.select("a > img").attr("data-lazyload").trim();
					} catch (Exception e) {
					}
				}
				if (!StringUtils.isBlank(headPhotoUrl)) {
					x.setHeadimg(headPhotoUrl);
				}		
				String xiaoquName = node.select("div.tit > a > h4").text().trim();
				if (AnalysisType.dp_hotel.equals(analysisType)) {
					xiaoquName = node.select("div.hotel-info-main > h2 > a.hotel-name-link").text().trim();
				}
				if (StringUtils.isBlank(xiaoquName)) {
					continue;
				}
				x.setXiaoquname(xiaoquName);
				String sourceUrl = node.select("div.pic > a").attr("abs:href").trim();
				if (AnalysisType.dp_hotel.equals(analysisType)) {
					sourceUrl = node.select("div.hotel-info-main > h2 > a.hotel-name-link").attr("abs:href").trim();
				}
				if (StringUtils.isBlank(sourceUrl)) {
					continue;
				}
				x.setFromurl(sourceUrl);
				String fromKey = sourceUrl.replaceAll("\\D+", "");
				x.setFromkey(fromKey);
				ImageClawer imageClawer = new ImageClawer(headPhotoUrl, TARGETID, TARGETINFO, fromKey, IMAGEDIRNAME);
				ImageClawer.ExecutorClaw(imageClawer);
				box.add(x);
			}
		}
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		Map<Params, String> paramsMap = new HashMap<Params, String>();
		paramsMap.put(Params.tempurl, "http://www.dianping.com/search/category/##/35/g2901/p$$");
		paramsMap.put(Params.area, "北京市");
		paramsMap.put(Params.xiaoquType, "4");
		paramsMap.put(Params.analysistype, AnalysisType.dp_general.toString());
		try {
			new DianPingListClawer(paramsMap).call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<XiaoQuEntity> call() throws Exception {
		return this.listAction();
	}

}
