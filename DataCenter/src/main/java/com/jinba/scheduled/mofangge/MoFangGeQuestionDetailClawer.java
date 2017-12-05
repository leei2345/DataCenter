package com.jinba.scheduled.mofangge;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

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
import com.jinba.pojo.ImageType;
import com.jinba.pojo.NewsEntity;
import com.jinba.pojo.QuestionSourceEntity;
import com.jinba.spider.core.ImageClawer;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * 2017-12-02T10:39:57.781114Z 1 [Note] A temporary password is generated for root@localhost: 6rNtA02Ihw<w
 * If you lose this password, please consult the section How to Reset the Root Password in the MySQL reference manual.
 * 
 * @author leei
 *
 */
public class MoFangGeQuestionDetailClawer extends BaseDetailClawer<QuestionSourceEntity> {

	private static final int TARGETID = 0;
	
	public MoFangGeQuestionDetailClawer(QuestionSourceEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
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
		String html = httpGet(url);
		return html;
	}

	@Override
	protected ActionRes analysistDetail(String html, DBHandle dbHandle) {
		if (StringUtils.isBlank(html)) {
			return ActionRes.ANALYSIS_HTML_NULL;
		}
		Document doc = Jsoup.parse(html, detailEntity.getFromurl());
		Elements questionTableNode = doc.select("div.detilinfo > table > tbody > tr");
		String questionStr = StringUtils.EMPTY;
		analysisNodes(questionStr, questionTableNode);
		
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
	
	public String analysisNodes (Element tdNodes) {
		String res = StringUtils.EMPTY;
		Elements divNode = tdNodes.getElementsByAttribute("div");
		if (divNode.size() > 0) {
			
		}
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
		BaseDetailClawer<NewsEntity> b = new MoFangGeQuestionDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
