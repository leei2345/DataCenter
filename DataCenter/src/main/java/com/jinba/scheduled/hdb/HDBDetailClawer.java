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
import com.alibaba.fastjson.JSONArray;
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

	//TODO 4
	private static final int TARGETID = 4;
	private static final String TARGETINFO = "hdb";
	private static final String IMAGEDIRNAME = "party";
	private static FastDateFormat sim = FastDateFormat.getInstance("yyyy-MM-dd 00:00:00");
	private static Pattern pattern = Pattern.compile("Lot=(\\d+\\.\\d+)\\&Lat=(\\d+\\.\\d+)");
	private static Pattern contentP = Pattern.compile("<script>\\s+.*?var _info\\s*=\\s*(.*);\\s+var appInfo=.*</script>");
	
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
		html = html.replace("data-src='http://cdn.hudongba.com/images3/yin.gif'", "");
		Matcher m = contentP.matcher(html);
		JSONObject contentObject = null;
		if (m.find()) {
			String contentJson = m.group(1);
			contentJson = contentJson.replaceAll(",//.*?\\s+", ", ");
			contentJson = contentJson.replace("无店铺为0", "").replace("未报名  1待支付 2已报名", "");
			contentJson = contentJson.replace("\\\"", "\"").replace("'", "");
			contentObject = JSONObject.parseObject(contentJson);
		} else {
			return ActionRes.ANALYSIS_FAIL;
		}
		Document doc = Jsoup.parse(html);
		Elements contentNodes = doc.select("div#dt_content>span");
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
				String text = node.html().trim();
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
		String partyStatus = "B";
		String statusStr = contentObject.getString("_infoState");
		if (StringUtils.equals("0", statusStr)) {
			partyStatus = "A";
		}
		this.detailEntity.setPartystatus(partyStatus);
		this.detailEntity.setIntro(content);
		String postTime = contentObject.getString("_pubDate");
		Date postTimeDate = Convert.parseDate(postTime);
		if (postTimeDate == null) {
			return ActionRes.ANALYSIS_FAIL;
		}
		postTime = sim.format(postTimeDate);
		this.detailEntity.setPosttime(postTime);
		String startDate = "";
		String endDate = "";
		String dateInfo = doc.select(".detail_Time_t>p").text();
		String[] dateInfoArr = dateInfo.split("~");
		if (dateInfoArr.length == 2) {
			startDate = dateInfoArr[0].trim();
			Date start = Convert.parseDate(startDate);
			startDate = sim.format(start);
			endDate = dateInfoArr[1].trim();
			Date end = Convert.parseDate(endDate);
			endDate = sim.format(end);
		}
		this.detailEntity.setBegintime(startDate);
		this.detailEntity.setEndtime(endDate);
		String deadLineDateStr = doc.select(".detail_Time_b>span").text();
		deadLineDateStr = deadLineDateStr.replace("报名截止", "");
		String deadLine = "";
		if (StringUtils.isNoneBlank(deadLineDateStr)) {
			Date deadLineDate = Convert.parseDate(deadLineDateStr);
			deadLine = sim.format(deadLineDate);
		}
		this.detailEntity.setDeadline(deadLine);
		String place = doc.select("div.detail_Attr a.detail_attr_blue").text().trim();
		if (!StringUtils.isBlank(place)) {
			this.detailEntity.setPlace(place);
		}
		String laloData = doc.select("div.detail_Attr a.detail_attr_blue").attr("href").trim();
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
		String userLimitStr = contentObject.getString("_personLimit");
		if (!StringUtils.isBlank(userLimitStr) && userLimitStr.matches("\\d+")) {
			int userlimit = Integer.parseInt(userLimitStr);
			this.detailEntity.setUserlimit(userlimit);
		}
		JSONArray priceArr = contentObject.getJSONArray("_payItemListJson");
		if (priceArr !=null && priceArr.size() > 0) {
			String feeStr = priceArr.getJSONObject(0).getString("price");
			BigDecimal fee = new BigDecimal(feeStr);
			this.detailEntity.setFee(fee);
			String feeInfo = priceArr.getJSONObject(0).getString("name");
			if (!StringUtils.isBlank(feeInfo)) {
				this.detailEntity.setFeedesc(feeInfo);
			}
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
		String json = "{\"areacode\":\"110108\",\"attendee\":\"\",\"begintime\":\"\",\"contact\":\"\",\"deadline\":\"\",\"endtime\":\"\",\"fee\":0,\"feedesc\":\"\",\"fromhost\":\"www.hdb.com\",\"fromkey\":\"hdb_uwrlu\",\"fromurl\":\"http://www.hdb.com/party/uyfdu.html?hdb_pos=find_rec\",\"headimg\":\"http://img.small.hudongba.com/upload/_oss/userpartyimg/201606/15/61465990918006_party6.png@!info-first-image\",\"intro\":\"\",\"latitude\":0,\"longitude\":0,\"organizer\":\"互助网\",\"parttype\":\"E\",\"partystatus\":\"\",\"partytime\":\"\",\"place\":\"\",\"posttime\":\"\",\"title\":\"白云峡谷捧河湾，溯溪捉虾拾贝，戏水狂欢\",\"userlimit\":0}";
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
