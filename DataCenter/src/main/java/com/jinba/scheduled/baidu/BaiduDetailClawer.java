package com.jinba.scheduled.baidu;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinba.core.BaseDetailClawer;
import com.jinba.core.DBHandle;
import com.jinba.pojo.ImageType;
import com.jinba.pojo.NewsEntity;
import com.jinba.spider.core.ImageClawer;
import com.jinba.spider.core.ImageParser;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 */
public class BaiduDetailClawer extends BaseDetailClawer<NewsEntity>{

	private static final int TARGETID = 3;
	private static final String TARGETINFO = "baidu";
	private static final String IMAGEDIRNAME = "news";
	private static final String URLHEAD = "http://m.baidu.com/news?tn=bdapiinstantfulltext&src=";
	private static final String BASEURL = "http://www.jinba.com";
	
	public BaiduDetailClawer(NewsEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
	}
	
	public BaiduDetailClawer(NewsEntity detailEntity) {
		super(TARGETID, detailEntity, new CountDownLatchUtils(1));
	}

	@Override
	protected ActionRes initParams() {
		return ActionRes.INITSUCC;
	}

	@Override
	protected String getDetailHtml() {
		String url = detailEntity.getFromurl();
		if (StringUtils.isBlank(url)) {
			return null;
		}
		try {
			url = URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		String fullUrl = URLHEAD + url;
		String html = httpGet(fullUrl);
		return html;
	}

	@Override
	protected ActionRes analysistDetail(String html, DBHandle dbHandle) {
		if (StringUtils.isBlank(html)) {
			return ActionRes.ANALYSIS_HTML_NULL;
		}
		String content = "";
		try {
			JSONObject obj = JSONObject.parseObject(html);
			JSONObject data = obj.getJSONObject("data").getJSONArray("news").getJSONObject(0);
			String nid = data.getString("nid");
			this.detailEntity.setFromkey(nid);
			JSONArray newContentArr = data.getJSONArray("content");
			int imageIndex = 1;
			for (int index = 0; index < newContentArr.size(); index++) {
				JSONObject eachContent = newContentArr.getJSONObject(index);
				String type = eachContent.getString("type");
				if (StringUtils.equals("image", type)) {
					String imageUrl = eachContent.getJSONObject("data").getJSONObject("original").getString("url");
					String imageDom = "<img src=\"" + imageUrl + "\" alt=\"img\" style=\"border:0;\"/>";
					imageDom = this.markdown(String.valueOf(imageIndex), imageDom, BASEURL);
					content += imageDom;
					imageIndex++;
				} else if (StringUtils.equals("text", type)) {
					String text = eachContent.getString("data");
					text = this.markdownText(text);
					content += text;
				}
			}
			detailEntity.setContent(content);
		} catch (Exception e) {
			return ActionRes.ANALYSIS_FAIL;
		}
		String selectSql = "select newsid from t_news where fromhost='" + detailEntity.getFromhost() + "' and fromkey='" + detailEntity.getFromkey() + "'";;
		List<Map<String, Object>> selectRes = dbHandle.select(selectSql);
		StringBuilder iubuilder = new StringBuilder();
		int id = 0;
		String imgurl = null;
		String areacode = null;
		boolean iuRes = false;
		if (selectRes != null && selectRes.size() > 0) {
			Map<String, Object> idMap = selectRes.get(0);
			long idL =  (Long) idMap.get("newsid");
			id = (int)idL;
			imgurl = detailEntity.getHeadimg();
			areacode = detailEntity.getAreacode();
			iubuilder.append("update t_news set ");
			iubuilder.append("areacode='" + areacode + "',");
			iubuilder.append("title='" + detailEntity.getTitle() + "',");
			iubuilder.append("content='" + detailEntity.getContent() + "',");
			iubuilder.append("headimg='" + imgurl + "',");
			iubuilder.append("source='" + detailEntity.getSource().replace(",", "，") + "',");
			iubuilder.append("newstime='" + detailEntity.getNewstime() + "',");
			iubuilder.append("posttime='" + detailEntity.getPosttime() + "',");
			iubuilder.append("fromhost='" + detailEntity.getFromhost() + "',");
			iubuilder.append("fromurl='" + detailEntity.getFromurl() + "',");
			iubuilder.append("updatetime=now() ");
			iubuilder.append("where fromkey='" + detailEntity.getFromkey() + "'");
			String updateSql = this.checkUpdateSql(iubuilder.toString());
			iuRes = dbHandle.update(updateSql);
		} else {
			iubuilder.append("insert into t_news (areacode,title,content,headimg,source,newstime,posttime,fromhost,fromurl,fromkey,updatetime) values (");
			iubuilder.append("'" + detailEntity.getAreacode() + "',");
			iubuilder.append("'" + detailEntity.getTitle() + "',");
			iubuilder.append("'" + detailEntity.getContent() + "',");
			iubuilder.append("'" + detailEntity.getHeadimg()+ "',");
			iubuilder.append("'" + detailEntity.getSource().replace(",", "，") + "',");
			iubuilder.append("'" + detailEntity.getNewstime() + "',");
			iubuilder.append("'" + detailEntity.getPosttime() + "',");
			iubuilder.append("'" + detailEntity.getFromhost() + "',");
			iubuilder.append("'" + detailEntity.getFromurl() + "',");
			iubuilder.append("'" + detailEntity.getFromkey() + "',");
			iubuilder.append("now())");
//			String insertSql = this.checkInsertSql(iubuilder.toString());
			id = dbHandle.insertAndGetId(iubuilder.toString());
			if (id > 0) {
				iuRes = true;
			}
			imgurl = detailEntity.getHeadimg();
			areacode = detailEntity.getAreacode();
		}
		if (!StringUtils.isBlank(imgurl) && iuRes) {
			String path = TARGETINFO + "/" + IMAGEDIRNAME + "/" + detailEntity.getAreacode() + "/";
			ImageClawer imgClawer = new ImageClawer(ImageType.EntityImage, imgurl, TARGETID, path, String.valueOf(id));
			imgClawer.addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 9_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13C75 Safari/601.1");
			ImageClawer.ExecutorClaw(imgClawer);
		}
		
		ActionRes res = null;
		if (iuRes) {
			res = ActionRes.ANALYSIS_SUCC;
		} else {
			res = ActionRes.DBHAND_FAIL;
		}
		return res;
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		/** 非酒店 */
		String json = "{\"areacode\":\"11011410\",\"content\":null,\"fromhost\":\"m.baidu.com\",\"fromkey\":null,\"fromurl\":\"http://elec.it168.com/a2016/0129/1902/000001902586.shtml\",\"headimg\":\"\",\"newstime\":\"2016-01-30 06:40:25\",\"posttime\":\"2016-01-30 17:15:19\",\"source\":\"企业网D1NET\",\"title\":\"沙河办证\"}";
		NewsEntity x = JSON.parseObject(json, NewsEntity.class);
		BaseDetailClawer<NewsEntity> b = new BaiduDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
