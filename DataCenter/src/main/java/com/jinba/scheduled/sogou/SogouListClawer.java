package com.jinba.scheduled.sogou;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jinba.core.BaseListClawer;
import com.jinba.pojo.NewsEntity;
import com.jinba.spider.core.Params;
import com.jinba.utils.Convert;
import com.jinba.utils.CountDownLatchUtils;

public class SogouListClawer extends BaseListClawer<NewsEntity> {

	private static final int TARGETID = 2;
	private static String tempUrl = "http://weixin.sogou.com/weixin?query=##&type=2&page=$$&ie=utf8";
	private static final String FROMHOST = "weixin.sogou.com";
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	private String areaName;
	private String areaCode;
	
	public SogouListClawer(Map<Params, String> paramsMap, CountDownLatchUtils cdl) {
		super(TARGETID, cdl);
		this.paramsMap = paramsMap;
		areaName = paramsMap.get(Params.area);
		areaCode = paramsMap.get(Params.citycode);
	}

	@Override
	protected ActionRes initParams() {
		return ActionRes.INITSUCC;
	}

	@Override
	protected void analysisAction(List<NewsEntity> box) {
		boolean next = true;
		int pageIndex = 1;
		String areaNameEn = new URLEncoder().encode(areaName);
		do {
			String url = tempUrl.replace("##", areaNameEn).replace("$$", String.valueOf(pageIndex));
			String html = httpGet(url);
			if (StringUtils.isBlank(html)) {
				break;
			}
			Document doc = Jsoup.parse(html, url);
			Elements nodes = doc.select("div.results > div[class=wx-rb wx-rb3]");
			for (Element element : nodes) {
				NewsEntity newsEntity = new NewsEntity();
				newsEntity.setAreacode(areaCode);
				newsEntity.setFromhost(FROMHOST);
				try {
					String title = element.select("div.txt-box > h4 > a").first().ownText().trim();
					newsEntity.setTitle(title);
				} catch (Exception e) {
					continue;
				}
				String fromUrl = element.select("div.txt-box > h4 > a").attr("abs:href").trim();
				newsEntity.setFromurl(fromUrl);
				String dateStr = element.select("div.txt-box > span.time").text();
				Date postDate = Convert.parseDate(dateStr);
			}
			pageIndex++;
		} while (next && pageIndex <= 10);
	}


}
