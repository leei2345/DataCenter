package com.jinba.scheduled.gongzhonghao;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.BasicCookieStore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

public class GongzhonghaoListClawer extends BaseListClawer<NewsEntity> implements Callable<List<NewsEntity>>{

	private static final int TARGETID = 2;
	private static final int OPTIONS = 256;
	private static String tempUrl = "http://weixin.sogou.com/weixin?type=1&query=$$&ie=utf8";
	private static final String FROMHOST = "weixin.sogou.com";
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	private static FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
	private String gongzhonghao;
	private String areaCode;
	private static Pattern msgPattern = Pattern.compile("var msgList\\s*=\\s*'(.*)';");
	
	public GongzhonghaoListClawer(Map<Params, String> paramsMap, CountDownLatchUtils cdl) {
		super(TARGETID, cdl);
		this.paramsMap = paramsMap;
		gongzhonghao = paramsMap.get(Params.gongzhonghao);
		areaCode = paramsMap.get(Params.citycode);
	}

	@Override
	protected ActionRes initParams() {
		return ActionRes.INITSUCC;
	}

	@Override
	protected void analysisAction(List<NewsEntity> box) {
		int pageIndex = 1;
		String gongzhonghaoEncode = new URLEncoder().encode(gongzhonghao);
		boolean next = true;
		do {
			SogouCookieEntity m = SogouCookieTask.getResource();
//			SogouCookieEntity m = new SogouCookieEntity();
			HttpHost proxy = m.getProxy();
			BasicCookieStore cookie = m.getCookie();
			next = false;
			String url = tempUrl.replace("$$", gongzhonghaoEncode);
	 		HttpMethod inner = new HttpMethod(targetId, cookie, proxy);
//	 		HttpMethod inner = new HttpMethod();
	 		inner.AddHeader(Method.Get, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
	 		inner.AddHeader(Method.Get, "Accept-Encoding", "gzip,deflate,sdch");
	 		inner.AddHeader(Method.Get, "Accept-Language", "zh-CN,zh;q=0.8");
	 		inner.AddHeader(Method.Get, "Cache-Control", "max-age=0");
	 		inner.AddHeader(Method.Get, "Connection", "keep-alive");
	 		inner.AddHeader(Method.Get, "Host", "weixin.sogou.com");
	 		inner.AddHeader(Method.Get, "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1736.2 Safari/537.36");
			String html = inner.GetHtml(url, HttpResponseConfig.ResponseAsStream);
			if (!StringUtils.isBlank(html) && !html.contains("您的访问过于频繁")) {
				logger.info("[Sogou Cookie Queue Available][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
			} else {
				logger.info("[Sogou Cookie Queue Unavailable][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
				m = SogouCookieTask.getResource();
				proxy = m.getProxy();
				cookie = m.getCookie();
				next = false;
		 		inner = new HttpMethod(targetId, cookie, proxy);
				html = inner.GetHtml(url, HttpResponseConfig.ResponseAsStream);
			}
			Document doc = Jsoup.parse(html, url);
			String pageInfo = doc.select("div[class=wx-rb bg-blue wx-rb_v1 _item]").attr("abs:href").trim();
			if (StringUtils.isBlank(pageInfo)) {
				break;
			}
			inner = new HttpMethod(targetId, cookie, proxy);
	 		String listHtml = inner.GetHtml(pageInfo, HttpResponseConfig.ResponseAsStream);
//	 		listHtml = listHtml.replace("sogou.weixin_gzhcb(", "").replaceAll("\\)$", "");
//	 		String xmlContent = "";
//	 		System.out.println(listHtml);
//	 		try {
//		 		listHtml = listHtml.replace("<![CDATA[", "").replace("]]>", "");
//		 		xmlContent = JSONObject.parseObject(listHtml).getString("items");
//	 		} catch (Exception e) {
//	 			break;
//	 		}
//	 		doc = Jsoup.parse(xmlContent);
	 		String msgJsonObjectStr = "";
	 		Matcher msgMatcher = msgPattern.matcher(listHtml);
	 		if (msgMatcher.find()) {
	 			msgJsonObjectStr = msgMatcher.group(1);
	 		} else {
	 			break;
	 		}
	 		msgJsonObjectStr = msgJsonObjectStr.replace("&quot;", "\"");
	 		JSONObject msgObject = JSONObject.parseObject(msgJsonObjectStr);
			JSONArray nodes = msgObject.getJSONArray("list");
			for (int index = 0; index < nodes.size(); index++) {
				JSONObject msgNode = nodes.getJSONObject(index).getJSONObject("app_msg_ext_info");
				NewsEntity newsEntity = new NewsEntity();
				newsEntity.setAreacode(areaCode);
				newsEntity.setFromhost(FROMHOST);
				try {
					String title = msgNode.getString("title").trim();
					newsEntity.setTitle(title);
				} catch (Exception e) {
					continue;
				}
				try {
					String content = msgNode.getString("digest").trim();
					newsEntity.setContent(content);
				} catch (Exception e) {
					continue;
				}
				String headimgurl = msgNode.getString("cover").trim();
				newsEntity.setHeadimg(headimgurl);
				String fromKey = msgNode.getString("fileid").trim();
				newsEntity.setFromkey(fromKey);
				String selectSql = "select newsid from t_news where fromhost='" + FROMHOST + "' and fromkey='" +fromKey + "'";;
				List<Map<String, Object>> selectRes = MysqlDao.getInstance().select(selectSql);
				if (selectRes != null && selectRes.size() > 0) {
					continue;
				}
				String fromUrl = msgNode.getString("content_url").trim();
				fromUrl = "http://mp.weixin.qq.com" + fromUrl.replace("\\", "").replace("amp;amp;", "");
				try {
					Thread.sleep(RandomUtils.nextLong(1000, 3000));
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
//				HttpMethod entityMe = new HttpMethod(TARGETID, cookie, proxy);
//				String entityRes = entityMe.GetLocationUrl(fromUrl);
//				if (entityRes.length() > 300) {
//					Matcher matcher = pattern.matcher(entityRes);
//					if (matcher.find()) {
//						entityRes = matcher.group(1);
//					}
//				}
				if (!StringUtils.isBlank(fromUrl) && !fromUrl.contains("antispider")) {
					try {
						URI uri = new URI(fromUrl);
						newsEntity.setFromurl(uri.toString());
						logger.info("[Sogou Cookie Queue Available][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
					} catch (Exception e) {
						logger.info("[Sogou Cookie Queue Unavailable][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
						continue;
					}
				} else {
					logger.info("[Sogou Cookie Queue Unavailable][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
					continue;
				}
				JSONObject dateObj = nodes.getJSONObject(index).getJSONObject("comm_msg_info");
				String dateStr = dateObj.getString("datetime").trim();
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
				newsEntity.setSource(gongzhonghao);
				newsEntity.setOptions(OPTIONS);
				box.add(newsEntity);
			}
			SogouCookieTask.returnResource(m);
			pageIndex++;
		} while (next && pageIndex <= 1);
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
		paramsMap.put(Params.gongzhonghao, "北青社区报上地版");
		paramsMap.put(Params.citycode, "11010201");
		try {
			List<NewsEntity> l = new GongzhonghaoListClawer(paramsMap, new CountDownLatchUtils(1)).listAction();
			for (NewsEntity newsEntity : l) {
				System.out.println(newsEntity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}


}
