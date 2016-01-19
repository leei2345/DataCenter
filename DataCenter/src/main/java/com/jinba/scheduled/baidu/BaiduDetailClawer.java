package com.jinba.scheduled.baidu;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.jinba.core.BaseDetailClawer;
import com.jinba.core.DBHandle;
import com.jinba.pojo.NewsEntity;
import com.jinba.spider.core.ImageClawer;
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
		return "Done";
	}

	@Override
	protected ActionRes analysistDetail(String html, DBHandle dbHandle) {
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
			ImageClawer imgClawer = new ImageClawer(imgurl, TARGETID, TARGETINFO, String.valueOf(id), areacode, IMAGEDIRNAME);
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
		String json = "{\"areacode\":\"110101\",\"fromhost\":\"weixin.sogou.com\",\"fromkey\":\"ab735a258a90e8e1-6bee54fcbd896b2a-2ae7e42d3f34733cd5d1b194ffd7250c\",\"fromurl\":\"http://mp.weixin.qq.com/s?__biz=MzA3NjI4ODUyMQ==&mid=401126035&idx=1&sn=6e6b7e0dd75706009fae9a57854c459a&3rd=MzA3MDU4NTYzMw==&scene=6#rd\",\"newstime\":\"2016-01-04 16:19:51\",\"posttime\":\"2016-01-04\",\"source\":\"廉政东城\",\"title\":\"东城区纪委书记谈“党风廉政建设和反腐败工作”\"}";
		NewsEntity x = JSON.parseObject(json, NewsEntity.class);
		BaseDetailClawer<NewsEntity> b = new BaiduDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
