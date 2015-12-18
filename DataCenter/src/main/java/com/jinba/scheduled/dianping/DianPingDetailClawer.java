package com.jinba.scheduled.dianping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jinba.core.BaseDetailClawer;
import com.jinba.core.DBHandle;
import com.jinba.pojo.XiaoQuEntity;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 */
public class DianPingDetailClawer extends BaseDetailClawer<XiaoQuEntity>{

	private static final int TARGETID = 1;
	private String sourceKey;
	private static final String IDENTIDY = "dp_";
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
		sourceKey = detailEntity.getFromkey().replace(IDENTIDY, "");;
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
		boolean isHotel = detailEntity.isHotel();
		String phone = null;
		if (isHotel) {
			String phoneStr = doc.select("div#hotel-intro > div.hotel-facilities > p >span").text().trim();
			Matcher matcher = phonePattern.matcher(phoneStr);
			if (matcher.find()) {
				phone = matcher.group(2);
			}
		} else {
			phone = doc.select("div.basic-info > p[class=expand-info tel] > span.item").text().trim();
		}
		JSONObject infoObject = JSONObject.parseObject(info);
		JSONObject shopInfoObject = infoObject.getJSONObject("msg").getJSONObject("shopInfo");
		int cityCode = shopInfoObject.getIntValue("cityId");
		String cityName = DianPingCityCode.getCityName(cityCode);
		if (StringUtils.isBlank(phone)) {
			phone = shopInfoObject.getString("phoneNo");
		}
		this.detailEntity.setPhone(phone);
		float glat = shopInfoObject.getFloat("glat");
		float glng = shopInfoObject.getFloatValue("glng");
		String address = shopInfoObject.getString("address");
		this.detailEntity.setLongItude(new BigDecimal(String.valueOf(glng)));
		this.detailEntity.setLatitude(new BigDecimal(String.valueOf(glat)));
		String areaName = null;
		if (isHotel) {
			areaName = doc.select("div[class=breadcrumb] > a:eq(1)").text().trim();
		} else {
			areaName = doc.select("div[class=expand-info address] > a[rel=nofollow] > span").text().trim();
		}
		areaName = areaName.replace("其他", "");
		String areaCode = DianPingCityMap.getAreaCodePro(areaName);
		boolean isQu = true;
		if (StringUtils.isBlank(areaCode)) {
			areaCode = DianPingCityMap.getAreaCode(areaName);
			isQu = false;
		}
		this.detailEntity.setAreacode(areaCode);
		if (isQu) {
			address = cityName + areaName + address;
		} else {
			address = cityName + address;
		}
		this.detailEntity.setAddress(address);
		String selectSql = "select xiaoquid from t_xiaoqu where fromkey='" + detailEntity.getFromkey() + "'";;
		List<Map<String, Object>> selectRes = dbHandle.select(selectSql);
		StringBuilder iubuilder = new StringBuilder();
		boolean iuRes = false;
		if (selectRes != null && selectRes.size() > 0) {
			iubuilder.append("update t_xiaoqu set ");
			iubuilder.append("areacode='" + detailEntity.getAreacode() + "',");
			iubuilder.append("xiaoquname='" + detailEntity.getXiaoquname() + "',");
			iubuilder.append("xiaoqutype='" + detailEntity.getXiaoquType() + "',");
			iubuilder.append("address='" + detailEntity.getAddress() + "',");
			iubuilder.append("longitude=" + detailEntity.getLongItude() + ",");
			iubuilder.append("latitude=" + detailEntity.getLatitude() + ",");
			iubuilder.append("phone='" + detailEntity.getPhone() + "',");
			iubuilder.append("headimg='" + detailEntity.getHeadimg() + "',");
			iubuilder.append("fromhost='" + detailEntity.getFromhost() + "',");
			iubuilder.append("fromurl='" + detailEntity.getFromurl() + "',");
			iubuilder.append("updatetime=now() ");
			iubuilder.append("where fromkey='" + detailEntity.getFromkey() + "'");
			String updateSql = this.checkUpdateSql(iubuilder.toString());
			iuRes = dbHandle.update(updateSql);
		} else {
			iubuilder.append("insert into t_xiaoqu (areacode,xiaoquname,xiaoqutype,address,longitude,latitude,phone,headimg,fromhost,fromurl,fromkey,updatetime,createtime) values (");
			iubuilder.append("'" + detailEntity.getAreacode() + "',");
			iubuilder.append("'" + detailEntity.getXiaoquname() + "',");
			iubuilder.append("'" + detailEntity.getXiaoquType() + "',");
			iubuilder.append("'" + detailEntity.getAddress() + "',");
			iubuilder.append("" + detailEntity.getLongItude() + ",");
			iubuilder.append("" + detailEntity.getLatitude() + ",");
			iubuilder.append("'" + detailEntity.getPhone() + "',");
			iubuilder.append("'" + detailEntity.getHeadimg() + "',");
			iubuilder.append("'" + detailEntity.getFromhost() + "',");
			iubuilder.append("'" + detailEntity.getFromurl() + "',");
			iubuilder.append("'" + detailEntity.getFromkey()+ "',now(),now())");
			iuRes = dbHandle.insert(iubuilder.toString());
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
		String json = "{\"address\":null,\"areacode\":null,\"createtime\":\"1970-01-01\",\"fromhost\":\"192.168.31.125\",\"fromkey\":\"dp_1769485\",\"fromurl\":\"http://www.dianping.com/shop/1768267\",\"headimg\":\"http://i1.s2.dpfile.com/pc/df9f6f962f08ccbc2fa9c7e9c55825f1(249x249)/thumb.jpg\",\"intro\":null,\"latitude\":0,\"longItude\":0,\"phone\":null,\"xiaoquType\":4,\"xiaoquname\":\"香山公园\"}";
		/** 酒店 */
//		String json = "{\"address\":null,\"areacode\":null,\"createtime\":\"1970-01-01\",\"fromhost\":\"192.168.31.125\",\"fromkey\":\"dp_1769485\",\"fromurl\":\"http://www.dianping.com/shop/2802772\",\"headimg\":\"http://i3.s2.dpfile.com/pc/9479d8318516cb5693d7cfdc5cd6a61a(240c180)/thumb.jpg\",\"hotel\":true,\"intro\":null,\"latitude\":0,\"longItude\":0,\"phone\":null,\"xiaoquType\":3,\"xiaoquname\":\"王府井希尔顿酒店\"}";
		XiaoQuEntity x = JSON.parseObject(json, XiaoQuEntity.class);
		BaseDetailClawer<XiaoQuEntity> b = new DianPingDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
