package com.jinba.scheduled.gongzhonghao;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
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
public class GongzhonghaoDetailClawer extends BaseDetailClawer<NewsEntity>{

	private static final int TARGETID = 2;
	private static final String TARGETINFO = "sogou";
	private static final String IMAGEDIRNAME = "news";
	
	public GongzhonghaoDetailClawer(NewsEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
	}
	
	public GongzhonghaoDetailClawer(NewsEntity detailEntity) {
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
			iubuilder.append("xiaoquid=" + detailEntity.getXiaoquid() + ",");
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
			inertParamsMap.put("xiaoquid", detailEntity.getXiaoquid(), false);
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
		String json = "{\"areacode\":\"11010803\",\"content\":\"审核人：辛颖撰稿人：编辑人：姜涛欢本文的亲们，请在页尾点赞哦~关注清华附中上地学校微信公众平台微信号：qhf\",\"fromhost\":\"weixin.sogou.com\",\"fromkey\":\"0c3eba2375111a86dfc9860a77a9ec59\",\"fromurl\":\"http://mp.weixin.qq.com/s?__biz=MjM5NzU0NzU5MA==&mid=2651695181&idx=1&sn=b27d7d9f67b46b6e76fe5c6e6d9f8fb0#rd\",\"headimg\":\"http://mmbiz.qpic.cn/mmbiz/WUQnwWm3zg24XYQxgHGu5riaNHPPYL3cRpWMcCcnDpt9YLAaLfSbXN0wJSFbCL5iaqkFm2pBibVpzGFtYoPzu5Zww/0?wx_fmt=jpeg\",\"newstime\":\"2016-06-16 14:14:25\",\"options\":256,\"posttime\":\"2016-06-16\",\"source\":\"清华大学附属中学上地学校\",\"title\":\"【清上新闻】春风化雨&nbsp;&nbsp;&nbsp;静待花开&nbsp;-----赵爱军老师政治学科开放型教学实践活动之分享会\",\"xiaoquid\":14092}";
		NewsEntity x = JSON.parseObject(json, NewsEntity.class);
		BaseDetailClawer<NewsEntity> b = new GongzhonghaoDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
