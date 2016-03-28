package com.jinba.scheduled.sogou;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
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
public class SogouDetailClawer extends BaseDetailClawer<NewsEntity>{

	private static final int TARGETID = 2;
	private static final String TARGETINFO = "sogou";
	private static final String IMAGEDIRNAME = "news";
	
	public SogouDetailClawer(NewsEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
	}
	
	public SogouDetailClawer(NewsEntity detailEntity) {
		super(TARGETID, detailEntity, new CountDownLatchUtils(1));
	}

	@Override
	protected ActionRes initParams() {
		return ActionRes.INITSUCC;
	}

	@Override
	protected String getDetailHtml() {
		return "Success";
	}

	@Override
	protected ActionRes analysistDetail(String html, DBHandle dbHandle) {
//		if (StringUtils.isBlank(html)) {
//			return ActionRes.ANALYSIS_HTML_NULL;
//		}
//		String content = "";
//		Document doc  = Jsoup.parse(html);
//		Elements pNodes = doc.select("div.rich_media_content >p");
//		for (int index = 0; index < pNodes.size(); index++) {
//			Element pNode = pNodes.get(index);
//			String pHtml = pNode.html();
//			String path = TARGETINFO + "/" + IMAGEDIRNAME + "/" + detailEntity.getAreacode();
//			String pContent = this.markdownContent(pHtml, path, BASEURL);
//			content += pContent;
//		}
//		if (!StringUtils.isBlank(content)) {
//			detailEntity.setContent(content);
//		}
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
			iubuilder.append("insert into t_news (areacode,title,content,headimg,source,newstime,posttime,fromhost,fromurl,fromkey,options,updatetime) values (");
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
			iubuilder.append("'" + detailEntity.getOptions() + "',");
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
		String json = "{\"areacode\":\"110101\",\"content\":\"为全面推动烟花爆竹消防安全管理工作深入开展,确保春节期间东华门地区良好的消防安全环境,近日,东华门街道召开东华门地区2016年春节烟花爆竹安全管理会议,驻区中央机关、市属机关、地区职能部门、地区重点单位和社区干部等共200余人参加会议. 首先,部署了春节烟花爆竹禁...\",\"fromhost\":\"weixin.sogou.com\",\"fromkey\":\"ab735a258a90e8e1-6bee54fcbd896b2a-09ebcb58240e9fde33fa88f0f2f7b090\",\"fromurl\":\"http://mp.weixin.qq.com/s?__biz=MzAwNzE5MTUzMg==&mid=401822212&idx=1&sn=71bd326fee3689a805b9e114bf0fe1b5&3rd=MzA3MDU4NTYzMw==&scene=6#rd\",\"headimg\":\"http://img01.sogoucdn.com/net/a/04/link?appid=100520031&url=http://mmbiz.qpic.cn/mmbiz/QqfrLiaa6IZENRF1Irjz8wySx86pbmfgia2iaXyFxnYlgUxIS3Uia3YEkZs7O2hay8nV5fvMliaMQN5HIvm5hyKiavoA/0?wx_fmt=jpeg\",\"newstime\":\"2016-01-31 21:23:22\",\"posttime\":\"2016-01-31\",\"source\":\"北京东城消防\",\"title\":\"东城区东华门街道召开2016年春节烟花爆竹安全管理会议\"}";
		NewsEntity x = JSON.parseObject(json, NewsEntity.class);
		BaseDetailClawer<NewsEntity> b = new SogouDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
