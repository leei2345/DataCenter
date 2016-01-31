package com.jinba.scheduled.sogou;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.BasicCookieStore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jinba.core.BaseListClawer;
import com.jinba.dao.MysqlDao;
import com.jinba.pojo.NewsEntity;
import com.jinba.pojo.SogouCookieEntity;
import com.jinba.scheduled.SogouCookieTask;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.Method;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;
import com.jinba.utils.LoggerUtil;

public class SogouListClawer extends BaseListClawer<NewsEntity> implements Callable<List<NewsEntity>>{

	private static final int TARGETID = 2;
	private static String tempUrl = "http://weixin.sogou.com/weixin?type=2&query=##&ie=utf8&sourceid=inttime_day&w=&sut=&sst0=&lkt=&page=$$";
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
			SogouCookieEntity m = SogouCookieTask.getResource();
//			SogouCookieEntity m = new SogouCookieEntity();
			HttpHost proxy = m.getProxy();
			BasicCookieStore cookie = m.getCookie();
			next = false;
			String url = tempUrl.replace("##", areaNameEn).replace("$$", String.valueOf(pageIndex));
	 		HttpMethod inner = new HttpMethod(targetId, cookie, proxy);
//	 		HttpMethod inner = new HttpMethod(targetId);
	 		inner.AddHeader(Method.Get, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
	 		inner.AddHeader(Method.Get, "Accept-Encoding", "gzip,deflate,sdch");
	 		inner.AddHeader(Method.Get, "Accept-Language", "zh-CN,zh;q=0.8");
	 		inner.AddHeader(Method.Get, "Cache-Control", "max-age=0");
	 		inner.AddHeader(Method.Get, "Connection", "keep-alive");
	 		inner.AddHeader(Method.Get, "Host", "weixin.sogou.com");
	 		inner.AddHeader(Method.Get, "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1736.2 Safari/537.36");
			String html = inner.GetHtml(url, HttpResponseConfig.ResponseAsStream);
//			String html = inner.GetHtml(url, HttpResponseConfig.ResponseAsStream);
			if (!StringUtils.isBlank(html) && !html.contains("您的访问过于频繁")) {
				LoggerUtil.ClawerInfoLog("[Sogou Cookie Queue Available][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
			} else {
				LoggerUtil.ClawerInfoLog("[Sogou Cookie Queue Unavailable][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
				m = SogouCookieTask.getResource();
				proxy = m.getProxy();
				cookie = m.getCookie();
				next = false;
		 		inner = new HttpMethod(targetId, cookie, proxy);
				html = inner.GetHtml(url, HttpResponseConfig.ResponseAsStream);
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
				try {
					String content = element.select("div.txt-box > p").first().text().trim();
					newsEntity.setContent(content);
				} catch (Exception e) {
					continue;
				}
				String headimgurl = element.select("div.img_box2 > a > img").attr("src").trim();
				newsEntity.setHeadimg(headimgurl);
				String fromKey = element.attr("d").trim();
				newsEntity.setFromkey(fromKey);
				String selectSql = "select newsid from t_news where fromhost='" + FROMHOST + "' and fromkey='" +fromKey + "'";;
				List<Map<String, Object>> selectRes = MysqlDao.getInstance().select(selectSql);
				if (selectRes != null && selectRes.size() > 0) {
					continue;
				}
				String fromUrl = element.select("div.txt-box > h4 > a").attr("abs:href").trim();
				try {
					Thread.sleep(RandomUtils.nextLong(1000, 4000));
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				HttpMethod entityMe = new HttpMethod(TARGETID, cookie, proxy);
				String entityRes = entityMe.GetLocationUrl(fromUrl);
				if (!StringUtils.isBlank(entityRes)) {
					try {
						URI uri = new URI(entityRes);
						newsEntity.setFromurl(uri.toString());
						LoggerUtil.ClawerInfoLog("[Sogou Cookie Queue Available][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
					} catch (Exception e) {
						LoggerUtil.ClawerInfoLog("[Sogou Cookie Queue Unavailable][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
						continue;
					}
				} else {
					continue;
				}
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
				newsEntity.setPosttime(today);
				String source = element.select("div.s-p > a#weixin_account").attr("title").trim();
				newsEntity.setSource(source);
				box.add(newsEntity);
			}
			SogouCookieTask.returnResource(m);
			pageIndex++;
		} while (next && pageIndex <= 2);
	}
	
	public List<NewsEntity> call() throws Exception {
		List<NewsEntity> list = this.listAction();
		return list;
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		Map<Params, String> paramsMap = new HashMap<Params, String>();
		paramsMap.put(Params.area, "东城区");
		paramsMap.put(Params.citycode, "110101");
		try {
			List<NewsEntity> l = new SogouListClawer(paramsMap, new CountDownLatchUtils(1)).listAction();
			for (NewsEntity newsEntity : l) {
				System.out.println(newsEntity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}


}
