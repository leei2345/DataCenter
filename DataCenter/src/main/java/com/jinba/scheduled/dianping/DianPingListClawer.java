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

import com.jinba.core.BaseListClawer;
import com.jinba.dao.MysqlDao;
import com.jinba.pojo.AnalysisType;
import com.jinba.pojo.XiaoQuEntity;
import com.jinba.scheduled.DianPingWorker;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 */
public class DianPingListClawer extends BaseListClawer<XiaoQuEntity> implements Runnable {

	private static final int TARGETID = 1;
	private String eachPageUrl;
	private int xiaoquType;
	private AnalysisType analysisType;
	private Map<Params, String> cityInfo = new HashMap<Params, String>();
	private static final String FROMHOST = "www.dianping.com";
	
	public DianPingListClawer (Map<Params, String> paramsMap, CountDownLatchUtils cdl) {
		super(TARGETID, cdl);
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
		paramsMap = null;
		return ActionRes.INITSUCC;
	}

	@Override
	protected void analysisAction(List<XiaoQuEntity> box) {
		HttpMethod getRegionMe = new HttpMethod(TARGETID);
		String getRegionHtml = getRegionMe.GetHtml(eachPageUrl.replace("p$$", ""), HttpResponseConfig.ResponseAsString);
		Document getRegionDoc = Jsoup.parse(getRegionHtml, eachPageUrl);
		Elements pageNode = getRegionDoc.select("div.page>a:nth-last-of-type(+2)");
		String regionPageCountStr = pageNode.text();
		int getRegionPageCount = 1;
		try {
			getRegionPageCount = Integer.parseInt(regionPageCountStr);
		} catch (Exception e) {
			System.err.println("[dianping claw " + cityInfo.get(Params.cityname) + " pagecount is 1]");
		}
		Elements regionNodes = getRegionDoc.select("div#region-nav>a");
		if (getRegionPageCount < 50 && regionNodes.size() > 0) {
			Element cityNode = regionNodes.first().clone();
			cityNode.attr("href", eachPageUrl);
			regionNodes.clear();
			regionNodes.add(cityNode);
		} 
		for (int regionIndex = 0; regionIndex < regionNodes.size(); regionIndex++) {
			Element regionNode = regionNodes.get(regionIndex);
			String regionUrl = regionNode.attr("abs:href");
			if (StringUtils.isBlank(regionUrl)) {
				continue;
			} else if (!regionUrl.contains("$$"))  {
				regionUrl = regionUrl.replaceAll("#.*$", "") + "p$$";
			}
			int pageCount = 1;
			for (int pageIndex = 1; pageIndex <= pageCount; pageIndex++) {
				String url = regionUrl.replace("$$", String.valueOf(pageIndex));
				HttpMethod m = new HttpMethod(TARGETID);
				String html = m.GetHtml(url, HttpResponseConfig.ResponseAsString);
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
					String xiaoquName = node.select("div.tit > a").attr("title").trim();
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
					String sql = "select xiaoquid from t_xiaoqu where fromhost='" + FROMHOST + "' and fromkey='" + fromKey + "'";
					List<Map<String, Object>> res = MysqlDao.getInstance().select(sql);
					if (res.size() > 0) {
						continue;
					}
					x.setFromkey(fromKey);
					box.add(x);
				}
			}
		}
		
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
		paramsMap.put(Params.tempurl, "http://www.dianping.com/search/category/##/75/g260p$$");
		paramsMap.put(Params.area, "延安市");
		paramsMap.put(Params.xiaoquType, "4");
		paramsMap.put(Params.analysistype, AnalysisType.dp_educate.toString());
		try {
			new DianPingListClawer(paramsMap, new CountDownLatchUtils(1)).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
