package com.jinba.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.jinba.dao.MysqlDao;
import com.jinba.pojo.SogouCookieEntity;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpRequestConfig;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.YunDaMa;
import com.jinba.utils.LoggerUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;


import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
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
	private static String reportMaRequestUrl = "http://weixin.sogou.com/antispider/thank.php";
	private static String weixinAntSpiderUrlHeader = "http://weixin.sogou.com/antispider/";
	private static String reportMaRequestBody = "c=$$&r=%2Fweixin%3Ftype%3D2%26query%3D%E4%B8%9C%E5%9F%8E%E5%8C%BA%26ie%3Dutf8%26w%3D%26sut%3D%26sst0%3D%26lkt%3D0%2C0%2C0&v=5";
	
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
				BasicCookieStore cookie = new BasicCookieStore();
				String step1Url = "http://pb.sogou.com/pv.gif?uigs_productid=webapp&type=antispider&subtype=close_refresh&domain=weixin&suv=&snuid=&t=" + System.currentTimeMillis();
				HttpMethod step1Me = new HttpMethod(TARGETID, cookie, proxy);
				step1Me.GetHtml(step1Url, HttpResponseConfig.ResponseAsStream);
				String spiderUrl = "http://weixin.sogou.com/antispider/?from=%2fweixin%3Ftype%3d2%26query%3d%E4%B8%9C%E5%9F%8E%E5%8C%BA%26ie%3dutf8%26_sug_%3dn%26_sug_type_%3d";
				HttpMethod spiderMe = new HttpMethod(TARGETID, cookie, proxy);
				html = spiderMe.GetHtml(spiderUrl, HttpResponseConfig.ResponseAsStream);
				Document doc = Jsoup.parse(html, URL);
				String imgUrl = doc.select("p.p4 > span.s1 > img").attr("src").trim();
				imgUrl = weixinAntSpiderUrlHeader + imgUrl;
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
					if (imgFile.exists()) {
						imgFile.delete();
					}
					continue;
				}
				String ma = maData[0];
				String cid = maData[1];
				String reuqestBody = reportMaRequestBody.replace("$$", ma);
				HttpMethod me = new HttpMethod(TARGETID, cookie, proxy);
				String reportMaResponse = me.GetHtml(reportMaRequestUrl, reuqestBody, HttpRequestConfig.RequestBodyAsString, HttpResponseConfig.ResponseAsStream);
				try {
					JSONObject object = JSONObject.parseObject(reportMaResponse);
					int code = object.getIntValue("code");
					String id = object.getString("id");
					if (code == 0 && !StringUtils.isBlank(id)) {
						cookie = me.getCookieStore();
						BasicClientCookie newCookie = new BasicClientCookie("SNUID", id);
						newCookie.setPath("/");
						newCookie.setDomain(".sogou.com");
						newCookie.setExpiryDate(new Date(System.currentTimeMillis() + 86400000));
						cookie.addCookie(newCookie);
						
						SogouCookieEntity cookieEntity = new SogouCookieEntity();
						cookieEntity.setCookie(cookie);
						cookieEntity.setProxy(proxy);
						cookieEntity.setCtime(ctime);
						cookieQueue.add(cookieEntity);
						
//						HttpMethod testMe = new HttpMethod(TARGETID, cookie, proxy);
//						String testHtml = testMe.GetHtml("http://weixin.sogou.com/weixin?type=2&query=%E4%B8%9C%E5%9F%8E%E5%8C%BA&ie=utf8&_sug_=n&_sug_type_=", HttpResponseConfig.ResponseAsStream);
//						System.out.println(testHtml);
						
						LoggerUtil.CookieInfoLog("[" + proxy.toHostString() + "][" + cookie.getCookies().toString() + "]");
						LoggerUtil.CookieInfoLog("[Sogou Cookie Product][Check Authcode Got New Cookie][Queue Size Is " + cookieQueue.size() + "]");
					} else {
						YunDaMa dama = new YunDaMa();
						dama.reportError(cid, ma);
					}
				} catch (Exception e) {
					continue;
				} finally {
					if (imgFile.exists()) {
						imgFile.delete();
					}
				}
				
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