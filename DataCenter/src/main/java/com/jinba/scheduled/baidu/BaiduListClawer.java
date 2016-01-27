package com.jinba.scheduled.baidu;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinba.core.BaseListClawer;
import com.jinba.pojo.NewsEntity;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

public class BaiduListClawer extends BaseListClawer<NewsEntity> implements Callable<List<NewsEntity>>{
	
	private static final int TARGETID = 3;//百度为3
	//##地区名称   $$替换当前第几条开始
	//http://m.baidu.com/news?tn=bdapinewsearch&word=%E4%B8%AD%E5%85%B3%E6%9D%91&pn=0&rn=20&ct=1
	private static String tempUrl = "http://m.baidu.com/news?tn=bdapinewsearch&word=##&pn=$$&rn=20&ct=1";
	
	private static final String FROMHOST = "m.baidu.com";
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	private static FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
	private String areaName;
	private String areaCode;
	
	public BaiduListClawer(Map<Params, String> paramsMap, CountDownLatchUtils cdl) {
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
			String url = tempUrl.replace("##", areaNameEn).replace("$$", String.valueOf(pageIndex * 20));
			//String html = httpGet(url);
			HttpMethod inner = new HttpMethod(targetId);
			String html = inner.GetHtml(url, HttpResponseConfig.ResponseAsStream);
			if (StringUtils.isBlank(html)) {
				break;
			}
			JSONObject infoObject = null;
			try {
				infoObject = JSONObject.parseObject(html);
				JSONArray listAll = infoObject.getJSONArray("list");
				for(int i = 0; i < listAll.size(); i++){
					JSONObject tempInfo = listAll.getJSONObject(i);
					NewsEntity bean = new NewsEntity();
					
					//资讯来源
					String source = tempInfo.getString("author");
					if(!StringUtils.isBlank(source)){
						bean.setSource(source);
					}
					String today = dateFormat.format(new Date());
					//资讯时间
					String newstimeStr = tempInfo.getString("sortTime");
					if(!StringUtils.isBlank(newstimeStr)){
						long newsTimeTemp = Long.parseLong(newstimeStr);
						String newstime = sim.format(new Date(newsTimeTemp * 1000));//资讯时间
						String newsdate = dateFormat.format(new Date(newsTimeTemp * 1000));//资讯时间的天数，木有时分秒
//						String fromKey = MD5.GetMD5Code(source + newstime);
	//					String fromKey = MD5Encoder.encode(new String(source + newstime).getBytes());
						//先判断数据库中是否存在这条数据，如果存在则不进行写入
//						String selectSql = "select newsid from t_news where fromhost='" + FROMHOST + "' and fromkey='" + fromKey + "'";;
//						List<Map<String, Object>> selectRes = MysqlDao.getInstance().select(selectSql);
//						if (selectRes != null && selectRes.size() > 0) {
//							continue;
//						}
						if(today.equals(newsdate)){//先根据时间去判断是否今天的，如果没有一条是今天的，则不进行抓取
							next = true;
							bean.setNewstime(newstime);//资讯时间
							bean.setPosttime(sim.format(new Date()));//发布时间为当前系统时间
//							bean.setFromkey(fromKey);//信息来源主键
						}else{
							continue;
						}
					}
					
					bean.setAreacode(areaCode);//地区代码
					bean.setFromhost(FROMHOST);//信息来源
					//资讯标题
					String title = tempInfo.getString("title");
					if(!StringUtils.isBlank(title)){
						title = title.replace("'", "‘").replace(",", "，");
						bean.setTitle(title);
					}
					//转自网页
					String fromurl = tempInfo.getString("url");
					if(!StringUtils.isBlank(fromurl)){
						bean.setFromurl(fromurl);
					}
					String imgUrl = tempInfo.getString("imgUrl");
					bean.setHeadimg(imgUrl);
					box.add(bean);
				}
				Thread.sleep(1000);
				pageIndex++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (next && pageIndex <= 10);
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
		paramsMap.put(Params.area, "沙河");
		paramsMap.put(Params.citycode, "11011410");
		try {
			List<NewsEntity> l = new BaiduListClawer(paramsMap, new CountDownLatchUtils(1)).listAction();
			for (NewsEntity newsEntity : l) {
				System.out.println(newsEntity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		long newsTimeTemp = Long.parseLong("1450773685");
//		String newstime = sim.format(new Date(newsTimeTemp * 1000));
//		System.err.println(newstime);
	}


}
