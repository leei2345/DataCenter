package com.jinba.scheduled.jinritoutiao;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jinba.core.BaseListClawer;
import com.jinba.pojo.NewsEntity;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

public class JinRiTouTiaoListClawer extends BaseListClawer<NewsEntity> implements Callable<List<NewsEntity>>{
	
	private static final int TARGETID = 4;//今日头条为4
	private static final int OPTIONS = 128;
	//##地区名称   $$替换当前第几条开始
	private static String TempUrl = "http://ic.snssdk.com/api/2/wap/search_content/?offset=$$&count=10&from=search_tab&keyword=##";
	
	private static final String FROMHOST = "ic.snssdk.com";
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");
	private static FastDateFormat dateTime = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	private static FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
	private String areaName;//地区名称
	private String areaCode;//地区ID
	
	public JinRiTouTiaoListClawer(Map<Params, String> paramsMap, CountDownLatchUtils cdl) {
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
		int pageIndex = 0;
		String areaNameEn = new URLEncoder().encode(areaName);
		boolean next = true;
		do {
			next = false;
			String url = TempUrl.replace("##", areaNameEn).replace("$$", String.valueOf(pageIndex * 10));
			JSONObject infoObject = null;
			try {
				HttpMethod inner = new HttpMethod(targetId);
				String html = inner.GetHtml(url, HttpResponseConfig.ResponseAsStream);
				if (StringUtils.isBlank(html)) {
					break;
				}
				infoObject = JSONObject.parseObject(html);
				String htmlAll = infoObject.getString("html");
				Document doc = Jsoup.parse(htmlAll);
				System.err.println(htmlAll);
				Elements nodes = doc.select("section:not([track-event])");
				for(Element element : nodes){
					NewsEntity bean = new NewsEntity();
					//资讯来源
					String source = element.select("section>a div.info>span[type=src]").text().trim();
					if(!StringUtils.isBlank(source)){
						bean.setSource(source);
					}
					
					String fromKey = element.select("section").attr("data-id").trim();
					try {
						String today = dateFormat.format(new Date());
						String newstimeStr = element.select("section>a div.info>span.time").attr("title").trim();//发布时间
						if(!StringUtils.isBlank(newstimeStr)){
							long newsTimeTemp = sim.parse(newstimeStr).getTime();
							String newstime = dateTime.format(new Date(newsTimeTemp));//资讯时间
							String newsdate = dateFormat.format(new Date(newsTimeTemp));//资讯时间的天数，木有时分秒
							if(today.equals(newsdate)){//先根据时间去判断是否今天的，如果没有一条是今天的，则不进行抓取
//								String fromKey = MD5.GetMD5Code(source + newstime.toString());
								bean.setFromkey(fromKey);//信息来源主键
								bean.setNewstime(newstime.toString());//资讯时间
								bean.setPosttime(sim.format(new Date()));//发布时间为当前系统时间
							}else{
								continue;
							}
						}
					} catch (Exception e) {
						logger.info(e.getMessage());
						continue;
					}
					
					//资讯标题
					try {
						String title = element.select("section>a h3").text().trim();
						bean.setTitle(title);
					} catch (Exception e) {
						logger.info(e.getMessage());
						continue;
					}
					//转自网页
					try {
						String fromurl = "http://toutiao.com/a" + fromKey + "/?app=news_article";
						bean.setFromurl(fromurl);
					} catch (Exception e) {
						logger.info(e.getMessage());
						continue;
					}
					
					bean.setAreacode(areaCode);//地区代码
					bean.setFromhost(FROMHOST);//信息来源
					bean.setOptions(OPTIONS);
					box.add(bean);
					String aa = JSON.toJSONString(bean);
					System.err.println("=======================" + aa);
				}
				pageIndex++;
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		} while (next && pageIndex <= 30);
	}
	
	public List<NewsEntity> call() throws Exception {
		List<NewsEntity> list = this.listAction();
		return list;
	}
	
	public static void main(String[] args) throws ParseException {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		Map<Params, String> paramsMap = new HashMap<Params, String>();
		paramsMap.put(Params.area, "东城区");
		paramsMap.put(Params.citycode, "110101");
		try {
			List<NewsEntity> l = new JinRiTouTiaoListClawer(paramsMap, new CountDownLatchUtils(1)).listAction();
			for (NewsEntity newsEntity : l) {
				System.out.println(newsEntity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
