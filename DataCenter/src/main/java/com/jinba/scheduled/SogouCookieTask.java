package com.jinba.scheduled;

import com.alibaba.fastjson.serializer.UUIDCodec;
import com.jinba.dao.MysqlDao;
import com.jinba.pojo.SogouCookieEntity;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.YunDaMa;
import com.jinba.utils.LoggerUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
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
	private static String reportMaRequestBody = "c=$$&r=%252Fweixin%253Ftype%253D2%2526query%253D%25E4%25B8%259C%25E5%259F%258E%25E5%258C%25BA%2526ie%253Dutf8%2526w%253D%2526sut%253D%2526sst0%253D%2526lkt%253D0%252C0%252C0&v=5";
	
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
			} else if (html.contains("过于频繁")) {
				Document doc = Jsoup.parse(html, URL);
				String imgUrl = doc.select("input[name=m] > span.s1 > img").attr("abs:src").trim();
				BasicCookieStore cookie = m.getCookieStore();
				m = new HttpMethod(TARGETID, cookie, proxy);
				byte[][] imgData = m.GetImageByteArr(imgUrl);
				String type = new String(imgData[1]);
				if (imgData == null || imgData[0] ==null || StringUtils.isBlank(type) || StringUtils.equals("txt", type)) {
					continue;
				}
				String fileName = UUID.randomUUID().toString() + "." + type;
				OutputStream imageStream = null;
				try {
					imageStream = new FileOutputStream(fileName);
					byte[] imageArr = imgData[0];
					imageStream.write(imageArr);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					continue;
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				} finally {
					if (imageStream != null) {
						try {
							imageStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				File imgFile = new File(fileName);
				YunDaMa y = new YunDaMa(imgFile);
				String[] maData = y.GetPhoneNumber();
				if (maData.length != 2) {
					continue;
				}
				String ma = maData[0];
				String cid = maData[1];
				String reuqestBody = reportMaRequestBody.replace("$$", ma);
				
				
				
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