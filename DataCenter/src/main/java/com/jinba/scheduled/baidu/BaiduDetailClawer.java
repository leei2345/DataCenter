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
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jinba.core.BaseDetailClawer;
import com.jinba.core.DBHandle;
import com.jinba.pojo.ImageType;
import com.jinba.pojo.NewsEntity;
import com.jinba.spider.core.ImageClawer;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 */
public class BaiduDetailClawer extends BaseDetailClawer<NewsEntity> {

	private static final int TARGETID = 3;
	private static final String TARGETINFO = "baidu";
	private static final String IMAGEDIRNAME = "news";
	private static final String URLHEAD = "http://m.baidu.com/news?tn=bdapiinstantfulltext&src=";
	
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
					String path = TARGETINFO + "/" + IMAGEDIRNAME + "/" + detailEntity.getAreacode() + "/";
					String imgName = this.detailEntity.getFromkey() + "_" + imageIndex;
					ImageClawer imgClawer = new ImageClawer(ImageType.EntityImage, imageUrl, 0, path, imgName);
					imgClawer.run();
					String completeImgName = imgClawer.getCompleteImgName();
					String imageDom = "<p style=\"text-indent:2em\" ><img src=\"http://www.jinba.com/baidu/news/" + this.detailEntity.getAreacode() + "/" + completeImgName + "\" alt=\"img\" /></p>";
					content += imageDom;
					imageIndex++;
				} else if (StringUtils.equals("text", type)) {
					String text = eachContent.getString("data");
					text = this.markdownText(text);
					content += text;
				}
			}
			if (StringUtils.isBlank(content)) {
				return ActionRes.ANALYSIS_FAIL;
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
			iubuilder.append("options='" + detailEntity.getOptions() + "',");
			iubuilder.append("updatetime=now() ");
			iubuilder.append("where fromkey='" + detailEntity.getFromkey() + "'");
			String updateSql = this.checkUpdateSql(iubuilder.toString());
			iuRes = dbHandle.update(updateSql);
		} else {
			Table<String, Object, Boolean> inertParamsMap = HashBasedTable.create();
			inertParamsMap.put("areacode", detailEntity.getAreacode(), true);
			inertParamsMap.put("title", detailEntity.getTitle(), true);
			inertParamsMap.put("content", detailEntity.getContent(), true);
			inertParamsMap.put("headimg", detailEntity.getHeadimg(), true);
			inertParamsMap.put("source", detailEntity.getSource(), true);
			inertParamsMap.put("newstime", detailEntity.getNewstime(), true);
			inertParamsMap.put("posttime", detailEntity.getPosttime(), true);
			inertParamsMap.put("fromhost", detailEntity.getFromhost(), true);
			inertParamsMap.put("fromurl", detailEntity.getFromurl(), true);
			inertParamsMap.put("fromkey", detailEntity.getFromkey(), true);
			inertParamsMap.put("options", detailEntity.getOptions(), true);
			inertParamsMap.put("updatetime", "now()", false);

			String insertSql = this.checkInsertSql("t_news", inertParamsMap);
			id = dbHandle.insertAndGetId(insertSql);
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
			imgClawer.run();
		}
		
		ActionRes res = null;
		if (iuRes) {
			res = ActionRes.ANALYSIS_SUCC;
		} else {
			res = ActionRes.DBHAND_FAIL;
		}
		return res;
	}
	
	@Override
	public void run() {
		super.run();
	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		/** 非酒店 */
		String json = "{\"areacode\":\"1201\",\"content\":\"\",\"fromhost\":\"m.baidu.com\",\"fromkey\":\"\",\"fromurl\":\"http://cpc.people.com.cn/n1/2017/0325/c117005-29168539.html\",\"headimg\":\"\",\"newstime\":\"2017-03-25 10:52:00\",\"options\":128,\"posttime\":\"2017-03-25 17:20:00\",\"source\":\"中国共产党新闻网\",\"title\":\"天津市领导干部学习贯彻党的十八届六中全会精神专题研讨班开班 ...\",\"xiaoquid\":0}";
		NewsEntity x = JSON.parseObject(json, NewsEntity.class);
		BaseDetailClawer<NewsEntity> b = new BaiduDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
