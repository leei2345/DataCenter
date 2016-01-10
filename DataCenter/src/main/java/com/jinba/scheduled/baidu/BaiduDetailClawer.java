package com.jinba.scheduled.baidu;

import java.util.List;
import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.jinba.core.BaseDetailClawer;
import com.jinba.core.DBHandle;
import com.jinba.pojo.NewsEntity;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 */
public class BaiduDetailClawer extends BaseDetailClawer<NewsEntity>{

	private static final int TARGETID = 3;
	
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
		boolean iuRes = false;
		if (selectRes != null && selectRes.size() > 0) {
			iubuilder.append("update t_news set ");
			iubuilder.append("areacode='" + detailEntity.getAreacode() + "',");
			iubuilder.append("title='" + detailEntity.getTitle() + "',");
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
			iubuilder.append("insert into t_news (areacode,title,source,newstime,posttime,fromhost,fromurl,fromkey,updatetime) values (");
			iubuilder.append("'" + detailEntity.getAreacode() + "',");
			iubuilder.append("'" + detailEntity.getTitle() + "',");
			iubuilder.append("'" + detailEntity.getSource().replace(",", "，") + "',");
			iubuilder.append("'" + detailEntity.getNewstime() + "',");
			iubuilder.append("'" + detailEntity.getPosttime() + "',");
			iubuilder.append("'" + detailEntity.getFromhost() + "',");
			iubuilder.append("'" + detailEntity.getFromurl() + "',");
			iubuilder.append("'" + detailEntity.getFromkey() + "',");
			iubuilder.append("now())");
			String insertSql = this.checkInsertSql(iubuilder.toString());
			iuRes = dbHandle.insert(insertSql);
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
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		/** 非酒店 */
		String json = "{\"areacode\":\"110101\",\"fromhost\":\"weixin.sogou.com\",\"fromkey\":\"ab735a258a90e8e1-6bee54fcbd896b2a-2ae7e42d3f34733cd5d1b194ffd7250c\",\"fromurl\":\"http://mp.weixin.qq.com/s?__biz=MzA3NjI4ODUyMQ==&mid=401126035&idx=1&sn=6e6b7e0dd75706009fae9a57854c459a&3rd=MzA3MDU4NTYzMw==&scene=6#rd\",\"newstime\":\"2016-01-04 16:19:51\",\"posttime\":\"2016-01-04\",\"source\":\"廉政东城\",\"title\":\"东城区纪委书记谈“党风廉政建设和反腐败工作”\"}";
		NewsEntity x = JSON.parseObject(json, NewsEntity.class);
		BaseDetailClawer<NewsEntity> b = new BaiduDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
