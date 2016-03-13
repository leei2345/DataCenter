package com.jinba.scheduled;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.SogouCookieEntity;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.utils.LoggerUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.RandomUtils;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.BasicCookieStore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SogouCookieTask implements Runnable {

	private static final int TARGETID = 2;
	private static final String URL = "http://weixin.sogou.com/weixin?type=2&query=%E4%B8%9C%E5%9F%8E%E5%8C%BA&ie=utf8&w=&sut=&sst0=&lkt=0%2C0%2C0";
	private static final long timeStep = 3600000L;
	private static LinkedBlockingQueue<SogouCookieEntity> cookieQueue = new LinkedBlockingQueue<SogouCookieEntity>();

	public void run() {
		Refresh();
	}
	
	public static void Refresh () {
		LoggerUtil.CookieInfoLog("[Sogou Cookie Product][Start]");
		String sql = "select host,port from tb_proxy_avail where target_id=2 and enable=1 order by u_time desc limit 1000";
		List<Map<String, Object>> proxyList = MysqlDao.getInstance().select(sql);
		for (Map<String, Object> map : proxyList) {
			String host = (String) map.get("host");
			int port = ((Integer) map.get("port")).intValue();
			HttpHost proxy = new HttpHost(host, port);
			HttpMethod m = new HttpMethod(TARGETID);
			m.setProxy(proxy);
			String html = m.GetHtml(URL, HttpResponseConfig.ResponseAsStream);
			long ctime = System.currentTimeMillis();
			if (html.contains("东城区")) {
				BasicCookieStore cookie = m.getCookieStore();
				Document doc = Jsoup.parse(html, URL);
				Elements nodes = doc.select("div.results > div[class=wx-rb wx-rb3]");
				int size = nodes.size() - 1;
				int index = RandomUtils.nextInt(1, size);
				Element node = nodes.get(index);
				String fromUrl = node.select("div.txt-box > h4 > a").attr("abs:href").trim();
				HttpMethod innerM = new HttpMethod(TARGETID, cookie, proxy);
				innerM.GetHtml(fromUrl, HttpResponseConfig.ResponseAsStream);
				cookie = innerM.getCookieStore();
				SogouCookieEntity cookieEntity = new SogouCookieEntity();
				cookieEntity.setCookie(cookie);
				cookieEntity.setProxy(proxy);
				cookieEntity.setCtime(ctime);
				cookieQueue.add(cookieEntity);
				LoggerUtil.CookieInfoLog("[" + proxy.toHostString() + "][" + cookie.getCookies().toString() + "]");
				LoggerUtil.CookieInfoLog("[Sogou Cookie Product][Added New Cookie][Queue Size Is " + cookieQueue.size() + "]");
			}
		}
	}

	public static synchronized SogouCookieEntity getResource() {
		SogouCookieEntity cookie = null;
		for (int index = 0; index < 10; index++) {
			cookie = (SogouCookieEntity) cookieQueue.poll();
			if (cookie == null) {
				synchronized (SogouCookieTask.class) {
					Refresh();
					break;
				}
			}
			long time = System.currentTimeMillis();
			if (time - cookie.getCtime() > timeStep) {
				cookie = null;
			} else {
				break;
			}
		}

		if (cookie == null) {
			cookie = cookieQueue.poll();
		}
		return cookie;
	}

	public static synchronized void returnResource(SogouCookieEntity cookie) {
		cookieQueue.add(cookie);
	}

	public static int getQueueSize() {
		return cookieQueue.size();
	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[] {"database.xml"});
		application.start();
		SogouCookieTask a = (SogouCookieTask) application.getBean("sogouCookieTask");
		a.run();
	}
}