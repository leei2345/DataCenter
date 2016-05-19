package com.jinba.scheduled.hdb;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jinba.core.BaseDetailClawer;
import com.jinba.core.DBHandle;
import com.jinba.pojo.ImageType;
import com.jinba.pojo.PartyEntity;
import com.jinba.spider.core.ImageClawer;
import com.jinba.utils.Convert;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 */
public class HDBDetailClawer extends BaseDetailClawer<PartyEntity> {

	private static final int TARGETID = 4;
	private static final String TARGETINFO = "hdb";
	private static final String IMAGEDIRNAME = "party";
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd 00:00:00");
	private static Pattern pattern = Pattern.compile("Lot=(\\d+\\.\\d+)\\&Lat=(\\d+\\.\\d+)");
	private static Pattern contentP = Pattern.compile("<script>var _info=(.*);\\(function\\(\\)\\{");
	
	public HDBDetailClawer(PartyEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
	}
	
	public HDBDetailClawer(PartyEntity detailEntity) {
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
		String html = httpGet(url);
		return html;
	}

	@Override
	protected ActionRes analysistDetail(String html, DBHandle dbHandle) {
		if (StringUtils.isBlank(html)) {
			return ActionRes.ANALYSIS_HTML_NULL;
		}
		Matcher m = contentP.matcher(html);
		JSONObject contentObject = null;
		if (m.find()) {
			String contentJson = m.group(1);
			contentJson = contentJson.replace("data-src=\\'http://img1.hudongba.com/images3/yin.gif\\'", "");
			contentObject = JSONObject.parseObject(contentJson);
		} else {
			return ActionRes.ANALYSIS_FAIL;
		}
		String contentHtml = contentObject.getString("_fileContnet");
		contentHtml = contentHtml.replace("data-src='http://cdn.hudongba.com/images3/yin.gif'", "");
		Document doc = Jsoup.parse(html);
		Document contentDoc = Jsoup.parse(contentHtml);
		Elements contentNodes = contentDoc.select("span");
		int imageIndex = 1;
		String content = "";
		for (int index = 0; index < contentNodes.size(); index++) {
			Element node = contentNodes.get(index);
			Element imageNode = null;
			try {
				imageNode = node.select("div.dt_content_pic > img").first();
			} catch (Exception e) {
			}
			if (imageNode == null) {
				String text = node.text().trim();
				if (!StringUtils.isBlank(text)) {
					text = this.markdownText(text);
					content += text;
				}
			} else {
				String imgUrl = imageNode.attr("data-src").trim();
				String path = TARGETINFO + "/" + IMAGEDIRNAME + "/" + detailEntity.getAreacode() + "/";
				String imgName = this.detailEntity.getFromkey() + "_" + imageIndex;
				ImageClawer imgClawer = new ImageClawer(ImageType.EntityImage, imgUrl, TARGETID, path, imgName);
				imgClawer.addHeader("Host", "img2.hudongba.com");
				imgClawer.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				imgClawer.addHeader("Accept-Encoding", "gzip,deflate,sdch");
				imgClawer.addHeader("Referer", this.detailEntity.getFromurl());
				imgClawer.run();
				String completeImgName = imgClawer.getCompleteImgName();
				if (!StringUtils.isBlank(completeImgName)) {
					String imageDom = "<p style=\"text-indent:2em\" ><img src=\"http://www.jinba.com/hdb/party/" + this.detailEntity.getAreacode() + "/" + completeImgName + "\" alt=\"img\" /></p>";
					content += imageDom;
				}
				imageIndex++;
			}
			
		}
		if (StringUtils.isBlank(content)) {
			return ActionRes.ANALYSIS_FAIL;
		}
		String partyStatus = "A";
		String statusStr = contentObject.getString("_state");
		if (StringUtils.equals("4", statusStr) || StringUtils.equals("5", statusStr)) {
			partyStatus = "B";
		}
		this.detailEntity.setPartystatus(partyStatus);
		this.detailEntity.setIntro(content);
		String dateInfo = doc.select("div.detail_Time_t > p").text().replace(" ", "").trim();
		dateInfo = dateInfo.replaceAll("\\(.*\\)", "").replace("/", "-");
		String postTime = doc.select("div.yhName > p.fbTime").text().trim();
		Date postTimeDate = Convert.parseDate(postTime);
		if (postTimeDate == null) {
			return ActionRes.ANALYSIS_FAIL;
		}
		postTime = sim.format(postTimeDate);
		this.detailEntity.setPosttime(postTime);
		String startDate = "";
		String endDate = "";
		if (StringUtils.isBlank(dateInfo)) {
			String deadLine = doc.select("div.detail_Time_b > p").text().trim();
			deadLine = deadLine.replace("报名截止", "").trim();
			Date deadLineDate = Convert.parseDate(deadLine);
			if (deadLineDate != null) {
				endDate = sim.format(deadLineDate);
			}
			startDate = postTime;
		} else {
			dateInfo = dateInfo.replace("开始", "").trim();
			this.detailEntity.setPartytime(dateInfo);
			String[] dateInfoArr = dateInfo.split("至");
			if (dateInfoArr.length == 1) {
				String start = dateInfoArr[0].trim();
				Date dateStart = Convert.parseDate(start);
				if (dateStart != null) {
					startDate = sim.format(dateStart);
				}
			} else if (dateInfoArr.length == 2) {
				String start = dateInfoArr[0].trim();
				String end = dateInfoArr[1].trim();
				Date dateStart = Convert.parseDate(start);
				if (dateStart != null) {
					startDate = sim.format(dateStart);
				}
				Date dateEnd = Convert.parseDate(end);
				if (dateEnd != null) {
					endDate = sim.format(dateEnd);
				}
			}
		}
		this.detailEntity.setDeadline(endDate);
		this.detailEntity.setBegintime(startDate);
		this.detailEntity.setEndtime(endDate);
		String place = doc.select("div.detail_Attr p[class~=addressP]").text().trim();
		if (!StringUtils.isBlank(place)) {
			this.detailEntity.setPlace(place);
		}
		String laloData = doc.select("div.detail_Attr > a").attr("href").trim();
		if (!StringUtils.isBlank(laloData)) {
			Matcher matcher = pattern.matcher(laloData);
			if (matcher.find()) {
				String la = matcher.group(2);
				String lo = matcher.group(1);
				BigDecimal lab = new BigDecimal(la);
				BigDecimal lob = new BigDecimal(lo);
				this.detailEntity.setLatitude(lab);
				this.detailEntity.setLongitude(lob);
			}
		}
		String userLimitStr = doc.select("div.detail_Joinnum_b > p > span").text().trim();
		if (!StringUtils.isBlank(userLimitStr) && userLimitStr.matches("\\d+")) {
			int userlimit = Integer.parseInt(userLimitStr);
			this.detailEntity.setUserlimit(userlimit);
		}
		String feeStr = doc.select("ul[class=ticket tc_c_feiLi] > li:eq(0)").attr("payitemprice").trim();
		if (!StringUtils.isBlank(feeStr) && feeStr.matches("\\d+")) {
			BigDecimal fee = new BigDecimal(feeStr);
			this.detailEntity.setFee(fee);
		}
		String feeInfo = doc.select("ul[class=ticket tc_c_feiLi] > li:eq(0) > div.ticket_all > div.ticket_des").text().trim();
		feeInfo = feeInfo.replace("请点击这里", "").replace(",", "");
		if (!StringUtils.isBlank(feeInfo)) {
			this.detailEntity.setFeedesc(feeInfo);
		}
		
		String selectSql = "select partyid from t_party where fromkey='" + detailEntity.getFromkey() + "'";;
		List<Map<String, Object>> selectRes = dbHandle.select(selectSql);
		StringBuilder iubuilder = new StringBuilder();
		int id = 0;
		String imgurl = null;
		String areacode = null;
		boolean iuRes = false;
		if (selectRes != null && selectRes.size() > 0) {
			Map<String, Object> idMap = selectRes.get(0);
			long idL =  (Long) idMap.get("partyid");
			id = (int)idL;
			imgurl = detailEntity.getHeadimg();
			areacode = detailEntity.getAreacode();
			iubuilder.append("update t_party set ");
			iubuilder.append("areacode='" + areacode + "',");
			iubuilder.append("partytype='" + detailEntity.getParttype() + "',");
			iubuilder.append("title='" + detailEntity.getTitle() + "',");
			iubuilder.append("intro='" + detailEntity.getIntro() + "',");
			iubuilder.append("headimg='" + imgurl + "',");
			iubuilder.append("posttime='" + detailEntity.getPosttime() + "',");
			iubuilder.append("deadline='" + detailEntity.getDeadline() + "',");
			iubuilder.append("begintime='" + detailEntity.getBegintime() + "',");
			iubuilder.append("endtime='" + detailEntity.getEndtime() + "',");
			iubuilder.append("partytime='" + detailEntity.getPartytime() + "',");
			iubuilder.append("latitude=" + detailEntity.getLatitude() + ",");
			iubuilder.append("longitude=" + detailEntity.getLongitude() + ",");
			iubuilder.append("place='" + detailEntity.getPlace() + "',");
			iubuilder.append("attendee='" + detailEntity.getAttendee() + "',");
			iubuilder.append("userlimit=" + detailEntity.getUserlimit() + ",");
			iubuilder.append("fee=" + detailEntity.getFee() + ",");
			iubuilder.append("feedesc='" + detailEntity.getFeedesc() + "',");
			iubuilder.append("organizer='" + detailEntity.getOrganizer() + "',");
			iubuilder.append("partystatus='" + detailEntity.getPartystatus() + "',");
			iubuilder.append("feedesc='" + detailEntity.getFeedesc() + "',");
			iubuilder.append("feedesc='" + detailEntity.getFeedesc() + "',");
			iubuilder.append("feedesc='" + detailEntity.getFeedesc() + "',");
			iubuilder.append("fromhost='" + detailEntity.getFromhost() + "',");
			iubuilder.append("fromurl='" + detailEntity.getFromurl() + "',");
			iubuilder.append("updatetime=now() ");
			iubuilder.append("where fromkey='" + detailEntity.getFromkey() + "'");
			String updateSql = this.checkUpdateSql(iubuilder.toString());
			iuRes = dbHandle.update(updateSql);
		} else {
			Table<String, Object, Boolean> inertParamsMap = HashBasedTable.create();
			inertParamsMap.put("areacode", detailEntity.getAreacode(), false);
			inertParamsMap.put("partytype", detailEntity.getParttype(), false);
			inertParamsMap.put("title", detailEntity.getTitle(), false);
			inertParamsMap.put("intro", detailEntity.getIntro(), false);
			inertParamsMap.put("areacode",	detailEntity.getAreacode(), true);
			inertParamsMap.put("partytype",	detailEntity.getParttype(), true);
			inertParamsMap.put("title",	detailEntity.getTitle(), true);
			inertParamsMap.put("intro",	detailEntity.getIntro(), true);
			inertParamsMap.put("headimg",	detailEntity.getHeadimg(), true);
			inertParamsMap.put("posttime",	detailEntity.getPosttime(), true);
			inertParamsMap.put("deadline",	detailEntity.getDeadline(), true);
			inertParamsMap.put("begintime",	detailEntity.getBegintime(), true);
			inertParamsMap.put("endtime",	detailEntity.getEndtime(), true);
			inertParamsMap.put("partytime",	detailEntity.getPartytime(), true);
			inertParamsMap.put("longitude",	detailEntity.getLongitude(), true);
			inertParamsMap.put("latitude",	detailEntity.getLatitude(), true);
			inertParamsMap.put("place",	detailEntity.getPlace(), true);
			inertParamsMap.put("userlimit",	detailEntity.getUserlimit(), true);
			inertParamsMap.put("fee",	detailEntity.getFee(), true);
			inertParamsMap.put("feedesc",	detailEntity.getFeedesc(), true);
			inertParamsMap.put("organizer",	detailEntity.getOrganizer(), true);
			inertParamsMap.put("partystatus",	detailEntity.getPartystatus(), true);
			inertParamsMap.put("fromhost",	detailEntity.getFromhost(), true);
			inertParamsMap.put("fromurl",	detailEntity.getFromurl(), true);
			inertParamsMap.put("fromkey",	detailEntity.getFromkey(), true);
			inertParamsMap.put("updatetime", "now()", false);
			
			String insertSql = this.checkInsertSql("t_party", inertParamsMap);
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
		String json = "{\"areacode\":\"110105\",\"attendee\":\"\",\"begintime\":\"\",\"contact\":\"\",\"deadline\":\"\",\"endtime\":\"\",\"fee\":0,\"feedesc\":\"\",\"fromhost\":\"www.hdb.com\",\"fromkey\":\"hdb_x2b7u\",\"fromurl\":\"http://www.hdb.com/party/x2b7u.html\",\"headimg\":\"http://img.small.hudongba.com/upload/_oss/userpartyimg/201604/12/41460472829677_party4.jpg@!info-first-image\",\"intro\":\"\",\"latitude\":0,\"longitude\":0,\"organizer\":\"北京枫林户外俱乐部\",\"parttype\":\"E\",\"partystatus\":\"\",\"partytime\":\"\",\"place\":\"\",\"posttime\":\"\",\"title\":\"【枫林户外】呼伦贝尔、莫尔道嘎、临江、室韦、额尔古纳、满洲里6日游\",\"userlimit\":0}";
		PartyEntity x = JSON.parseObject(json, PartyEntity.class);
		BaseDetailClawer<PartyEntity> b = new HDBDetailClawer(x, new CountDownLatchUtils(1));
		
		
//		Table<String, Object, Boolean> inertParamsMap = HashBasedTable.create();
//		inertParamsMap.put("areacode", "", false);
//		inertParamsMap.put("partytype", "sdsd", false);
//		inertParamsMap.put("title", 1, false);
//		inertParamsMap.put("fromkey", "ssd's'dsd", true);
//		inertParamsMap.put("updatetime", "now()", false);
//		
//		System.out.println(b.checkInsertSql("dsd", inertParamsMap));
		
		
		b.detailAction();
	}


}
