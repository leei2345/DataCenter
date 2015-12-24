package com.jinba.scheduled.sogou;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jinba.core.BaseListClawer;
import com.jinba.pojo.AnalysisType;
import com.jinba.pojo.NewsEntity;
import com.jinba.scheduled.dianping.DianPingListClawer;
import com.jinba.spider.core.Params;
import com.jinba.utils.Convert;
import com.jinba.utils.CountDownLatchUtils;

public class SogouListClawer extends BaseListClawer<NewsEntity>{

	private static final int TARGETID = 2;
	private static String tempUrl = "http://weixin.sogou.com/weixin?type=2&query=##&ie=utf8&w=&sut=&sst0=&lkt=&page=$$";
	private static final String FROMHOST = "weixin.sogou.com";
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	private static FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
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
		int pageIndex = 1;
		String areaNameEn = new URLEncoder().encode(areaName);
		boolean next = true;
		do {
			next = false;
			String url = tempUrl.replace("##", areaNameEn).replace("$$", String.valueOf(pageIndex));
			String firstUrl = "http://weixin.sogou.com/pcindex/pc/web/web.js?t=" + System.currentTimeMillis();
			this.addGetHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			this.addGetHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1736.2 Safari/537.36");
			this.addGetHeader("Accept-Encoding", "gzip,deflate,sdch");
			this.addGetHeader("Accept-Language", "zh-CN,zh;q=0.8");
			this.addGetHeader("Cache-Control", "max-age=0");
			this.addGetHeader("Connection", "keep-alive");
			System.out.println(httpGetCookie(firstUrl));
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
					String title = element.select("div.txt-box > h4 > a").first().text().trim();
					newsEntity.setTitle(title);
				} catch (Exception e) {
					continue;
				}
				String fromUrl = element.select("div.txt-box > h4 > a").attr("abs:href").trim();
				newsEntity.setFromurl(fromUrl);
				String dateStr = element.select("div.txt-box > div.s-p").attr("t").trim();
				long time = 0l;
				try {
					time = Long.parseLong(dateStr);
				} catch (Exception e) {
					continue;
				}
				Date postDate = new Date(time*1000);
				String postDateStr = dateFormat.format(postDate);
				String today = dateFormat.format(new Date());
				if (today.equals(postDateStr)) {
					next = true;
				} else {
					continue;
				}
				newsEntity.setNewstime(sim.format(postDate));
				String source = element.select("div.s-p > a#weixin_account").attr("title").trim();
				newsEntity.setSource(source);
				String fromKey = element.attr("d").trim();
				newsEntity.setFromkey(fromKey);
				box.add(newsEntity);
			}
			pageIndex++;
		} while (next && pageIndex <= 10);
	}

	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		Map<Params, String> paramsMap = new HashMap<Params, String>();
		paramsMap.put(Params.area, "北京市");
		paramsMap.put(Params.citycode, "1101");
		try {
			new SogouListClawer(paramsMap, new CountDownLatchUtils(1)).listAction();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

}
