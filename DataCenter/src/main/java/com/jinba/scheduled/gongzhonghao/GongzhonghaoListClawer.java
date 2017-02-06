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
import com.jinba.pojo.NewsEntity;
import com.jinba.pojo.SogouCookieEntity;
import com.jinba.scheduled.SogouCookieTask;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.Method;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;
import com.jinba.utils.MD5;

public class GongzhonghaoListClawer extends BaseListClawer<NewsEntity> implements Callable<List<NewsEntity>>{

	private static final int TARGETID = 2;
	private static final int OPTIONS = 256;
	private static String tempUrl = "http://weixin.sogou.com/weixin?type=1&query=$$&ie=utf8";
	private static final String FROMHOST = "weixin.sogou.com";
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	private static FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
	private String gongzhonghao;
	private String areaCode;
	private int xiaoquid;
	private static Pattern msgPattern = Pattern.compile("var msgList\\s*=\\s*'(.*)';");
	private static Pattern pattern = Pattern.compile("var msg_link\\s*=\\s*\"(.*)\";");
	
	public GongzhonghaoListClawer(Map<Params, String> paramsMap, CountDownLatchUtils cdl) {
		super(TARGETID, cdl);
		this.paramsMap = paramsMap;
		gongzhonghao = paramsMap.get(Params.gongzhonghao);
		areaCode = paramsMap.get(Params.citycode);
		String xiaoquidStr = paramsMap.get(Params.xiaoquid);
		xiaoquid = Integer.parseInt(xiaoquidStr);
	}

	@Override
	protected ActionRes initParams() {
		return ActionRes.INITSUCC;
	}

	@Override
	protected void analysisAction(List<NewsEntity> box) {
		String gongzhonghaoEncode = new URLEncoder().encode(gongzhonghao);
			SogouCookieEntity m = SogouCookieTask.getResource();
//			SogouCookieEntity m = new SogouCookieEntity();
			HttpHost proxy = m.getProxy();
			BasicCookieStore cookie = m.getCookie();
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
		 		inner = new HttpMethod(targetId, cookie, proxy);
				html = inner.GetHtml(url, HttpResponseConfig.ResponseAsStream);
			}
			Document doc = Jsoup.parse(html, url);
			String pageInfo = doc.select("div[class=wx-rb bg-blue wx-rb_v1 _item]").attr("abs:href").trim();
			if (StringUtils.isBlank(pageInfo)) {
				return;
			}
			inner = new HttpMethod(targetId, cookie, proxy);
	 		String listHtml = inner.GetHtml(pageInfo, HttpResponseConfig.ResponseAsStream);
	 		String msgJsonObjectStr = "";
	 		Matcher msgMatcher = msgPattern.matcher(listHtml);
	 		if (msgMatcher.find()) {
	 			msgJsonObjectStr = msgMatcher.group(1);
	 		} else {
	 			return;
	 		}
	 		msgJsonObjectStr = msgJsonObjectStr.replace("&quot;", "\"");
	 		JSONObject msgObject = JSONObject.parseObject(msgJsonObjectStr);
			JSONArray nodes = msgObject.getJSONArray("list");
			if (nodes.size() == 0) {
				return;
			}
			for (int nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
				JSONObject todayObject = nodes.getJSONObject(nodeIndex);
				JSONObject dateObj = todayObject.getJSONObject("comm_msg_info");
				String dateStr = dateObj.getString("datetime").trim();
				long time = 0l;
				try {
					time = Long.parseLong(dateStr);
				} catch (Exception e) {
					continue;
				}
				Date postDate = new Date(time*1000);
				String postDateStr = dateFormat.format(postDate);
				JSONObject msgNode = todayObject.getJSONObject("app_msg_ext_info");
				NewsEntity newsEntity = new NewsEntity();
				newsEntity.setAreacode(areaCode);
				newsEntity.setFromhost(FROMHOST);
				newsEntity.setXiaoquid(xiaoquid);
				try {
					String title = msgNode.getString("title").trim();
					newsEntity.setTitle(title);
					String fromKey = MD5.GetMD5Code(areaCode + title);
					newsEntity.setFromkey(fromKey);
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
				headimgurl = headimgurl.replace("\\/", "/");
				newsEntity.setHeadimg(headimgurl);
				String fromUrl = msgNode.getString("content_url").trim();
				fromUrl = "http://mp.weixin.qq.com" + fromUrl.replace("\\", "").replace("amp;amp;", "");
				try {
					Thread.sleep(RandomUtils.nextLong(1000, 2000));
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				HttpMethod entityMe = new HttpMethod(TARGETID, cookie, proxy);
				String entityRes = entityMe.GetLocationUrl(fromUrl);
				if (entityRes.length() > 300) {
					Matcher matcher = pattern.matcher(entityRes);
					if (matcher.find()) {
						entityRes = matcher.group(1);
						entityRes = entityRes.replace("&amp;", "&");
					}
				}
				if (!StringUtils.isBlank(entityRes) && !entityRes.contains("antispider")) {
					try {
						entityRes = entityRes.replace("amp;", "");
						URI uri = new URI(entityRes);
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
				newsEntity.setNewstime(sim.format(postDate));
				newsEntity.setPosttime(postDateStr);
				newsEntity.setSource(gongzhonghao);
				newsEntity.setOptions(OPTIONS);
				box.add(newsEntity);
				JSONArray multiArray = msgNode.getJSONArray("multi_app_msg_item_list");
				for (int index = 0; index < multiArray.size(); index++) {
					JSONObject innerObj = multiArray.getJSONObject(index);
					NewsEntity innerEntity = new NewsEntity();
					innerEntity.setAreacode(areaCode);
					innerEntity.setFromhost(FROMHOST);
					innerEntity.setXiaoquid(xiaoquid);
					try {
						String title = innerObj.getString("title").trim();
						innerEntity.setTitle(title);
						String innerfromKey = MD5.GetMD5Code(areaCode + title);
						innerEntity.setFromkey(innerfromKey);
					} catch (Exception e) {
						continue;
					}
					try {
						String content = innerObj.getString("digest").trim();
						innerEntity.setContent(content);
					} catch (Exception e) {
						continue;
					}
					String innerheadimgurl = innerObj.getString("cover").trim();
					innerheadimgurl = innerheadimgurl.replace("\\/", "/");
					innerEntity.setHeadimg(innerheadimgurl);
					String innerfromUrl = innerObj.getString("content_url").trim();
					innerfromUrl = "http://mp.weixin.qq.com" + innerfromUrl.replace("\\", "").replace("amp;amp;", "");
					try {
						Thread.sleep(RandomUtils.nextLong(1000, 2000));
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					HttpMethod innerEntityMe = new HttpMethod(TARGETID, cookie, proxy);
					String innerEntityRes = innerEntityMe.GetLocationUrl(innerfromUrl);
					if (innerEntityRes.length() > 300) {
						Matcher matcher = pattern.matcher(innerEntityRes);
						if (matcher.find()) {
							innerEntityRes = matcher.group(1);
						}
					}
					if (!StringUtils.isBlank(innerEntityRes) && !innerEntityRes.contains("antispider")) {
						try {
							innerEntityRes = innerEntityRes.replace("amp;", "");
							URI uri = new URI(innerEntityRes);
							innerEntity.setFromurl(uri.toString());
							logger.info("[Sogou Cookie Queue Available][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
						} catch (Exception e) {
							logger.info("[Sogou Cookie Queue Unavailable][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
							continue;
						}
					} else {
						logger.info("[Sogou Cookie Queue Unavailable][Cookie Queue Size Is " + SogouCookieTask.getQueueSize() + "]");
						continue;
					}
					innerEntity.setNewstime(sim.format(postDate));
					innerEntity.setPosttime(postDateStr);
					innerEntity.setSource(gongzhonghao);
					innerEntity.setOptions(OPTIONS);
					box.add(innerEntity);
				}
			}
			SogouCookieTask.returnResource(m);
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
		paramsMap.put(Params.gongzhonghao, "通州小兵");
		paramsMap.put(Params.citycode, "11010803");
		paramsMap.put(Params.xiaoquid, "14092");
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
