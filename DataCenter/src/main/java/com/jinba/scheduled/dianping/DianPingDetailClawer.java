package com.jinba.scheduled.dianping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jinba.core.BaseDetailClawer;
import com.jinba.core.DBHandle;
import com.jinba.pojo.AnalysisType;
import com.jinba.pojo.XiaoQuEntity;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 */
public class DianPingDetailClawer extends BaseDetailClawer<XiaoQuEntity>{

	private static final int TARGETID = 1;
	private String sourceKey;
	private static Pattern phonePattern = Pattern.compile("电话\\s?(:|：){1}\\s?(\\d+-?\\d+)\\s*");
	
	public DianPingDetailClawer(XiaoQuEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
	}

	@Override
	protected ActionRes initParams() {
		return ActionRes.INITSUCC;
	}

	@Override
	protected String getDetailHtml() {
		sourceKey = detailEntity.getFromkey();
		String url = "http://www.dianping.com/shop/" + sourceKey;
		String html = httpGet(url);
		return html;
	}

	@Override
	protected ActionRes analysistDetail(String html, DBHandle dbHandle) {
		long timeStemp = System.currentTimeMillis();
		String url = "http://www.dianping.com/ajax/json/shop/wizard/BasicHideInfoAjaxFP?_nr_force=" + timeStemp + "&shopId=" + sourceKey;
		String info = httpGet(url);
		if (StringUtils.isBlank(info)) {
			return ActionRes.ANALYSIS_FAIL;
		}
		Document doc = Jsoup.parse(html);
		AnalysisType analysisType = detailEntity.getAnalysisType();
		String phone = null;
		Elements areaNameNodes = null;
		if (AnalysisType.dp_hotel.equals(analysisType)) {
			String phoneStr = doc.select("div#hotel-intro > div.hotel-facilities > p >span").text().trim();
			Matcher matcher = phonePattern.matcher(phoneStr);
			if (matcher.find()) {
				phone = matcher.group(2);
			}
			areaNameNodes = doc.select("div.breadcrumb > a");
		} else if (AnalysisType.dp_trade.equals(analysisType)) {
			phone = doc.select("div.market-detail > div.market-detail-other > p:has(span:contains(联系电话))").text().trim();
			phone = phone.replace("联系电话： ", "");
			areaNameNodes = doc.select("div.breadcrumb > a");
		} else if (AnalysisType.dp_educate.equals(analysisType)) {
			phone = doc.select("div.brief-info > div.phone > span.item").text().trim();
			areaNameNodes = doc.select("div.breadcrumb > div.inner > a");
		} else {
			phone = doc.select("div.basic-info > p[class=expand-info tel] > span.item").text().trim();
			areaNameNodes = doc.select("div.breadcrumb > a");
		}
		JSONObject infoObject = JSONObject.parseObject(info);
		JSONObject shopInfoObject = infoObject.getJSONObject("msg").getJSONObject("shopInfo");
		if (StringUtils.isBlank(phone)) {
			phone = shopInfoObject.getString("phoneNo");
		}
		this.detailEntity.setPhone(phone);
		float glat = shopInfoObject.getFloat("glat");
		float glng = shopInfoObject.getFloatValue("glng");
		String address = shopInfoObject.getString("address");
		this.detailEntity.setLongItude(new BigDecimal(String.valueOf(glng)));
		this.detailEntity.setLatitude(new BigDecimal(String.valueOf(glat)));
		String areaCode = null;
		List<String> areaNameList = new ArrayList<String>();
		for (int index = areaNameNodes.size() - 1; index >=0; index--) {
			Element node = areaNameNodes.get(index);
			String areaName = node.text();
			areaNameList.add(areaName);
			areaName = areaName.replace("其他", "");
			String[] innerArr = areaName.split("/");
			for (String inner : innerArr) {
				if (StringUtils.isBlank(areaCode)) {
					areaCode = DianPingCityMap.getShangquanCode(inner);
					if (StringUtils.isBlank(areaCode)) {
						areaCode = DianPingCityMap.getChengQuCode(inner);
						if (StringUtils.isBlank(areaCode)) {
							areaCode = DianPingCityMap.getAreaCode(inner);
						}
					}
				} 
			}
		}
		if (StringUtils.isBlank(areaCode)) {
			areaCode = this.detailEntity.getCityInfo().get(Params.citycode);
		} 
		String comments = "";
		for (int areaNameIndex = areaNameList.size() - 1; areaNameIndex >=0; areaNameIndex--) {
			comments += (">" + areaNameList.get(areaNameIndex));
		}
		comments = comments.replaceFirst(">", "");
		this.detailEntity.setComments(comments);
		this.detailEntity.setAreacode(areaCode);
		this.detailEntity.setAddress(address);
		String selectSql = "select xiaoquid from t_xiaoqu where fromhost='" + detailEntity.getFromhost() + "' and fromkey='" + detailEntity.getFromkey() + "'";;
		List<Map<String, Object>> selectRes = dbHandle.select(selectSql);
		StringBuilder iubuilder = new StringBuilder();
		boolean iuRes = false;
		if (selectRes != null && selectRes.size() > 0) {
			iubuilder.append("update t_xiaoqu set ");
			iubuilder.append("areacode='" + detailEntity.getAreacode() + "',");
			iubuilder.append("xiaoquname='" + detailEntity.getXiaoquname().replace(",", "，") + "',");
			iubuilder.append("xiaoqutype='" + detailEntity.getXiaoquType() + "',");
			iubuilder.append("address='" + detailEntity.getAddress().replace(",", "，") + "',");
			iubuilder.append("longitude=" + detailEntity.getLongItude() + ",");
			iubuilder.append("latitude=" + detailEntity.getLatitude() + ",");
			iubuilder.append("phone='" + detailEntity.getPhone() + "',");
			iubuilder.append("headimg='" + detailEntity.getHeadimg() + "',");
			iubuilder.append("fromhost='" + detailEntity.getFromhost() + "',");
			iubuilder.append("fromurl='" + detailEntity.getFromurl() + "',");
			iubuilder.append("comments='" + detailEntity.getComments() + "',");
			iubuilder.append("updatetime=now() ");
			iubuilder.append("where fromkey='" + detailEntity.getFromkey() + "'");
			String updateSql = this.checkUpdateSql(iubuilder.toString());
			iuRes = dbHandle.update(updateSql);
		} else {
			iubuilder.append("insert into t_xiaoqu (areacode,xiaoquname,xiaoqutype,address,longitude,latitude,phone,headimg,fromhost,fromurl,fromkey,comments,updatetime,createtime) values (");
			iubuilder.append("'" + detailEntity.getAreacode() + "',");
			iubuilder.append("'" + detailEntity.getXiaoquname().replace(",", "，") + "',");
			iubuilder.append("'" + detailEntity.getXiaoquType() + "',");
			iubuilder.append("'" + detailEntity.getAddress().replace(",", "，") + "',");
			iubuilder.append("" + detailEntity.getLongItude() + ",");
			iubuilder.append("" + detailEntity.getLatitude() + ",");
			iubuilder.append("'" + detailEntity.getPhone() + "',");
			iubuilder.append("'" + detailEntity.getHeadimg() + "',");
			iubuilder.append("'" + detailEntity.getFromhost() + "',");
			iubuilder.append("'" + detailEntity.getFromurl() + "',");
			iubuilder.append("'" + detailEntity.getFromkey()+ "','" + detailEntity.getComments() + "',now(),now())");
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
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		/** 非酒店 */
		String json = "{\"address\":null,\"analysisType\":\"dp_general\",\"areacode\":null,\"cityInfo\":{\"citycode\":\"1101\",\"cityname\":\"北京市\"},\"createtime\":\"1970-01-01\",\"fromhost\":\"www.dianping.com\",\"fromkey\":\"14695025\",\"fromurl\":\"http://www.dianping.com/shop/14695025\",\"headimg\":\"http://i3.s2.dpfile.com/pc/9479d8318516cb5693d7cfdc5cd6a61a(240c180)/thumb.jpg\",\"intro\":null,\"latitude\":0,\"longItude\":0,\"phone\":null,\"xiaoquType\":3,\"xiaoquname\":\"新城滨河森林公园\"}";
		/** 酒店 */
//		String json = "{\"address\":null,\"areacode\":null,\"createtime\":\"1970-01-01\",\"fromhost\":\"192.168.31.125\",\"fromkey\":\"dp_1769485\",\"fromurl\":\"http://www.dianping.com/shop/2802772\",\"headimg\":\"http://i3.s2.dpfile.com/pc/9479d8318516cb5693d7cfdc5cd6a61a(240c180)/thumb.jpg\",\"hotel\":true,\"intro\":null,\"latitude\":0,\"longItude\":0,\"phone\":null,\"xiaoquType\":3,\"xiaoquname\":\"王府井希尔顿酒店\"}";
		/** 购物 */
//		String json = "{\"address\":null,\"analysisType\":\"dp_trade\",\"areacode\":null,\"cityInfo\":{\"cityname\":\"北京市\",\"citycode\":\"1101\"},\"createtime\":\"1970-01-01\",\"fromhost\":\"192.168.31.125\",\"fromkey\":\"dp_3671260\",\"fromurl\":\"http://www.dianping.com/shop/3671260\",\"headimg\":\"http://i2.s2.dpfile.com/pc/3c8d1955223a314afc2cd8252f243447(249x249)/thumb.jpg\",\"intro\":null,\"latitude\":0,\"longItude\":0,\"phone\":null,\"xiaoquType\":3,\"xiaoquname\":\"侨福芳草地购物中心\"}";
		/** 教育 */
//		String json = "{\"address\":null,\"analysisType\":\"dp_educate\",\"areacode\":null,\"cityInfo\":{\"cityname\":\"北京市\",\"citycode\":\"1101\"},\"createtime\":\"1970-01-01\",\"fromhost\":\"192.168.31.125\",\"fromkey\":\"dp_9038774\",\"fromurl\":\"http://www.dianping.com/shop/9038774\",\"headimg\":\"http://qcloud.dpfile.com/pc/-nOtzMv4FfTWzVnJMlCS-KBXk48sDcT_0mXM99h9Lhqy1L-OvjVbIaCzC0mKpWbuGlZteH1Hr2Hx2EhJBCuTMw.jpg\",\"intro\":null,\"latitude\":0,\"longItude\":0,\"phone\":null,\"xiaoquType\":4,\"xiaoquname\":\"爱迪国际学校\"}";
		XiaoQuEntity x = JSON.parseObject(json, XiaoQuEntity.class);
		BaseDetailClawer<XiaoQuEntity> b = new DianPingDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
