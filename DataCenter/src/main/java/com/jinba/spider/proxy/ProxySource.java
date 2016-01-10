//package com.jinba.spider.proxy;
//
//import java.util.Date;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.time.StopWatch;
//
//import com.jinba.utils.LoggerUtil;
//
//
//public class ProxySource implements Runnable {
//	
//	private static String baseUrl = "http://dev.kuaidaili.com/api/getproxy/?orderid=984790248908740&num=200&b_pcchrome=1&b_pcie=1&b_pcff=1&protocol=1&method=2&sp1=1&sp2=1&quality=1&sort=0&dedup=1&format=text&sep=1";
//	private static Pattern pattern = Pattern.compile("((\\d{1,3}\\.){3}\\d{1,3}:\\d+)");
//	
//	public String GetHtml () {
//		HttpHandler http = new HttpHandler();
////		http.AddHeader("Host", "erwx.daili666.com");
//		http.AddHeader("User-Agent",	"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0");
//		http.AddHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//		http.AddHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
////		http.AddHeader("Accept-Encoding", "gzip, deflate");
//		http.AddHeader("Connection", "keep-alive");
//		http.AddHeader("If-None-Match", "");
//		http.AddHeader("Cache-Control", "");
//		String html = http.httpGet(baseUrl, "UTF-8").getHtml();
//		return html;
//	}
//	
//	/**
//	 * 
//	 * @param html
//	 * @return 获取到的IP数量
//	 */
//	public int Handle (String html) {
//		if (StringUtils.isBlank(html) || StringUtils.equals("没有找到符合条件的IP", html)) {
//			LoggerUtil.ProxyLog("[ProxySource][" + new Date() + "][Fail]");
//			return 0;
//		}
//		int succCount = 0;
//		String[] data = html.split("\n");
//		for (String str : data) {
//			Matcher matcher = pattern.matcher(str);
//			if (matcher.find()) {
//				String each = matcher.group(1);
//				String[] arr = each.split(":");
//				String host = arr[0];
//				int port = 0;
//				try {
//					port = Integer.parseInt(arr[1]);
//				} catch (Exception e) {
//					continue;
//				}
//				String selectSql = "select source from available_proxy_ip where ip='" + host + "' and port=" + port;
//				Rs rs = DataBaseCenter.GetDao().rs(selectSql);
//				if (rs.isNull()) {
//					String sql = "insert into available_proxy_ip (source,ip,port,response_time,create_time,update_time) values ('kuaidaili','" + host + "'," + port + ",9999,now(),now())";
//					int count = DataBaseCenter.GetDao().exec(sql);
//					if (count > 0) {
//						succCount++;
//					} 
//				}
//			}
//		}
//		return succCount;
//	}
//
//	@Override
//	public void run() {
//		MornitorEntity mornitor = new MornitorEntity("ProxySource");
//		StopWatch watch = new StopWatch();
//		watch.start();
//		int count = 0;
//		LoggerUtil.ProxyLog("[ProxySource][Scheduled Start]");
//		try {
//			String html = this.GetHtml();
//			count = this.Handle(html);
//			LoggerUtil.ProxyLog("[ProxySource][Scheduled Done][Succ][Get " + count + "]");
//		} catch (Exception e) {
//			LoggerUtil.ProxyLog("[ProxySource][" + new Date() + "][" + e.toString() + "][Get " + count + "]");
//		}
//		long time = watch.getTime();
//		mornitor.AddTime(time);
//		mornitor.Count(true, count);
//		mornitor.MakeDB();
//	}
//	
//	public static void main(String[] args) {
//		ProxySource proxy = new ProxySource();
//		new Thread(proxy).start();
//	}
//
//}
