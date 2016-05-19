package com.jinba.scheduled.hdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jinba.core.BaseListClawer;
import com.jinba.dao.MysqlDao;
import com.jinba.pojo.PartyEntity;
import com.jinba.scheduled.AreaInfoMap;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

public class HdbListClawer extends BaseListClawer<PartyEntity> implements Callable<List<PartyEntity>> {

	private static final int TARGETID = 4;
	private static final String TARGETINFO = "hdb";
	private String eachPageUrl;
	private String partyType;
	private String areaCode;
	private static final String FROMHOST = "www.hdb.com";
	private static final Pattern pattern = Pattern.compile("http://www.hdb.com/party/(.*?)\\.html");
	
	public HdbListClawer (Map<Params, String> paramsMap, CountDownLatchUtils cdl) {
		super(TARGETID, cdl);
		this.paramsMap = paramsMap;
	}
	
	@Override
	protected ActionRes initParams() {
		String city = paramsMap.get(Params.area);
		areaCode = HdbCityMap.getAreaCode(city);
		String tempUrl = paramsMap.get(Params.tempurl);
		partyType = paramsMap.get(Params.parttype);
		eachPageUrl = tempUrl.replace("@@", city);
		paramsMap = null;
		return ActionRes.INITSUCC;
	}

	@Override
	protected void analysisAction(List<PartyEntity> box) {
		int pageIndex = 1;
		boolean hasNext = false;
		do {
			hasNext = false;
			String url = eachPageUrl.replace("$$", String.valueOf(pageIndex));
			HttpMethod method = new HttpMethod(TARGETID);
//			HttpMethod method = new HttpMethod();
			String html = method.GetHtml(url, HttpResponseConfig.ResponseAsStream);
			Document doc = Jsoup.parse(html, url);
			Elements nodes = doc.select("ul.find_main_ul > li.find_main_li");
			for (Element node : nodes) {
				PartyEntity x = new PartyEntity();
				x.setParttype(partyType);
				x.setFromhost(FROMHOST);
				String title = node.select("div.find_main_title > a > h4").text().trim();
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
				Matcher matcher = pattern.matcher(sourceUrl);
				String fromKey = null;
				if (matcher.find()) {
					fromKey = matcher.group(1);
				} else {
					continue;
				}
				fromKey = TARGETINFO + "_" + fromKey;
				//查询数据库去重
				String sql = "select partyid from t_party where fromkey='" + fromKey + "'";
				List<Map<String, Object>> existList = MysqlDao.getInstance().select(sql);
				if (existList.size() == 0) {
					hasNext = true;
				}
				x.setFromkey(fromKey);
				
				String imgUrl = node.select("> a > img").attr("data-src").trim();
				if (!StringUtils.isBlank(imgUrl)) {
					x.setHeadimg(imgUrl);
				}
				String organizer = node.select("div.find_main_div > div.find_main_b a.hd_mem_name_A").text().trim();
				x.setOrganizer(organizer);
				box.add(x);
			}
			pageIndex++;
		} while (hasNext && pageIndex <12);
	}
	
	public List<PartyEntity> call() {
		List<PartyEntity> list = this.listAction();
		return list;
	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		Map<Params, String> paramsMap = new HashMap<Params, String>();
		paramsMap.put(Params.tempurl, "http://www.hdb.com/find/@@-flmu-sjbx-p$$/");
		paramsMap.put(Params.area, "beijing");
		paramsMap.put(Params.parttype, "E");
		try {
			new HdbListClawer(paramsMap, new CountDownLatchUtils(1)).call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
