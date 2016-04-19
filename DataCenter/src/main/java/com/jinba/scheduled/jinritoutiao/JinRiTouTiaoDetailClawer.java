package com.jinba.scheduled.jinritoutiao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.sound.midi.SysexMessage;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jinba.core.BaseDetailClawer;
import com.jinba.core.DBHandle;
import com.jinba.core.BaseClawer.ActionRes;
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
public class JinRiTouTiaoDetailClawer extends BaseDetailClawer<NewsEntity>{

	private static final int TARGETID = 4;
	private static final String TARGETINFO = "toutiao";
	private static final String IMAGEDIRNAME = "news";
	private static final String URLHEAD = "";
	private static final String BASEURL = "http://toutiao.com";
	
	public JinRiTouTiaoDetailClawer(NewsEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
	}
	
	public JinRiTouTiaoDetailClawer(NewsEntity detailEntity) {
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
		String fullUrl = URLHEAD + url;
		String html = httpGet(fullUrl);
		return html;
	}

	@Override
	protected ActionRes analysistDetail(String html, DBHandle dbHandle) {
		if (StringUtils.isBlank(html)) {
			return ActionRes.ANALYSIS_HTML_NULL;
		}
		StringBuffer content = new StringBuffer();
		int modelType = 1;//模板类型，由于他有两个模板，所以暂定1为模板1,2为模板2，以此类推
		try {
			System.err.println(html);
			Document doc = Jsoup.parse(html);
			String path = TARGETINFO + "/" + IMAGEDIRNAME + "/" + detailEntity.getAreacode() + "/";//存放图片的路径
			Elements pNodes = doc.select("div.article-content > *");
			if(pNodes == null || pNodes.size() <= 0){
				modelType = 2;
				pNodes = doc.select("article >p");
			}
			if(pNodes == null || pNodes.size() <= 0){
				return ActionRes.ANALYSIS_FAIL;
			}
			if(pNodes.html().contains("视频加载中...")){
				return ActionRes.ANALYSIS_FAIL;
			}
			Elements title = doc.select("div.article-header >h1,header>h1");
			if(title != null && title.size() >= 0){
				content.append("<h1>" + title.html() + "</h1>");
			}
			int imageIndex = 1;
			for (int index = 0; index < pNodes.size(); index++) {
				String imgName = this.detailEntity.getFromkey() + "_" + imageIndex;//图片名称
				Element pNode = pNodes.get(index);
				String pHtml = pNode.html();
				boolean isImgType = false;//判断是否包含图片；如果包含，则获取出来图片地址做跟换
				if (pNode.tagName().equals("img") || pHtml.contains("<img ")){
					isImgType = true;
				}
				String imageUrl = "";
				if(isImgType){//包含图片
					if(modelType == 1){
						imageUrl = pNode.attr("src");
					}else if(modelType == 2){
						imageUrl = pNode.select("img").attr("alt_src");
					}
					ImageClawer imgClawer = new ImageClawer(ImageType.EntityImage, imageUrl, 0, path, imgName);
					imgClawer.run();
					String completeImgName = imgClawer.getCompleteImgName();
					detailEntity.setHeadimg("http://www.jinba.com/" + TARGETINFO + "/" + IMAGEDIRNAME + "/" + this.detailEntity.getAreacode() + "/" + completeImgName);
					String imageDom = "<p style=\"text-indent:2em\" ><img src=\"http://www.jinba.com/" + TARGETINFO + "/" + IMAGEDIRNAME + "/" + this.detailEntity.getAreacode() + "/" + completeImgName + "\" alt=\"img\" /></p>";
					imageIndex++;
					content.append(imageDom);
				}else{//不包含图片
					String text = pNode.outerHtml().trim();
					text = this.markdownText(text);
					content.append(text);
				}
			}
			if (!StringUtils.isBlank(content)) {
				detailEntity.setContent(content.toString());
			}
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
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		/** 非酒店 */
		String json = "{\"areacode\":\"110101\",\"fromhost\":\"ic.snssdk.com\",\"fromkey\":\"6273302695101530370\",\"fromurl\":\"http://toutiao.com/a6271095929072910593/?app=news_article\",\"newstime\":\"2016-04-14 15:06:00\",\"posttime\":\"2016-04-17 14:27\",\"source\":\"中国网\",\"title\":\"北京市东城区板厂小学美术老师温昱\"}";
		NewsEntity x = JSON.parseObject(json, NewsEntity.class);
		BaseDetailClawer<NewsEntity> b = new JinRiTouTiaoDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
